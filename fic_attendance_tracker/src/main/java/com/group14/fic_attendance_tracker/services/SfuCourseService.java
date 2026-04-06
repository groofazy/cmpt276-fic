package com.group14.fic_attendance_tracker.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group14.fic_attendance_tracker.models.Course;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SfuCourseService {
    private static final URI OUTLINES_URI = URI.create("https://api.sfucourses.com/v1/rest/outlines");
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private volatile CachedCatalog cachedCatalog = new CachedCatalog(List.of(), Map.of(), Instant.EPOCH);

    public SfuCourseService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public List<String> getDepartments() {
        return new ArrayList<>(getCatalog().departments());
    }

    public List<String> getCourseNumbers(String department) {
        if (department == null || department.isBlank()) {
            return List.of();
        }

        return new ArrayList<>(getCatalog().courseNumbersByDepartment().getOrDefault(department, List.of()));
    }

    private CachedCatalog getCatalog() {
        CachedCatalog snapshot = cachedCatalog;
        if (!snapshot.isExpired()) {
            return snapshot;
        }

        synchronized (this) {
            snapshot = cachedCatalog;
            if (!snapshot.isExpired()) {
                return snapshot;
            }

            cachedCatalog = loadCatalog();
            return cachedCatalog;
        }
    }

    private CachedCatalog loadCatalog() {
        HttpRequest request = HttpRequest.newBuilder(OUTLINES_URI)
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return cachedCatalog.departments().isEmpty() ? new CachedCatalog(List.of(), Map.of(), Instant.now()) : cachedCatalog;
            }

            JsonNode root = objectMapper.readTree(response.body());
            if (!root.isArray()) {
                return cachedCatalog.departments().isEmpty() ? new CachedCatalog(List.of(), Map.of(), Instant.now()) : cachedCatalog;
            }

            Set<String> allowedDepartments = Arrays.stream(Course.CourseSubject.values())
                    .map(Enum::name)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            Map<String, Set<String>> numbersByDepartment = new HashMap<>();
            for (JsonNode outline : root) {
                String dept = outline.path("dept").asText("").trim();
                String number = outline.path("number").asText("").trim();

                if (dept.isEmpty() || number.isEmpty() || !allowedDepartments.contains(dept)) {
                    continue;
                }

                String normalizedNumber = number.replaceAll("[^0-9]", "");
                if (normalizedNumber.isEmpty()) {
                    continue;
                }

                try {
                    numbersByDepartment.computeIfAbsent(dept, ignored -> new LinkedHashSet<>())
                            .add(String.format("%03d", Integer.parseInt(normalizedNumber)));
                } catch (NumberFormatException exception) {
                    continue;
                }
            }

            List<String> departments = numbersByDepartment.keySet().stream()
                    .sorted()
                    .toList();

            Map<String, List<String>> courseNumbersByDepartment = numbersByDepartment.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().stream().sorted(Comparator.naturalOrder()).toList(),
                            (left, right) -> left,
                            HashMap::new
                    ));

            return new CachedCatalog(departments, courseNumbersByDepartment, Instant.now());
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while loading SFU course catalog", exception);
        }
    }

    private record CachedCatalog(List<String> departments, Map<String, List<String>> courseNumbersByDepartment, Instant loadedAt) {
        private boolean isExpired() {
            return loadedAt == null || loadedAt.plus(CACHE_TTL).isBefore(Instant.now());
        }
    }
}