package yunrry.flik.core.service.embedding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VectorProcessingService {

    private final OpenAIEmbeddingService embeddingService;

    public Mono<String> createLocationEmbedding(BigDecimal latitude, BigDecimal longitude) {
        double normalizedLat = normalizeLatitude(latitude.doubleValue());
        double normalizedLng = normalizeLongitude(longitude.doubleValue());

        return Mono.just(String.format("[%f,%f]", normalizedLat, normalizedLng));
    }

    public Mono<String> createTagEmbedding(String tag1, String tag2, String tag3,
                                           String tags, String labelDepth1,
                                           String labelDepth2, String labelDepth3) {
        List<String> allTags = List.of(tag1, tag2, tag3, tags, labelDepth1, labelDepth2, labelDepth3)
                .stream()
                .filter(tag -> tag != null && !tag.trim().isEmpty())
                .collect(Collectors.toList());

        String combinedText = String.join(" ", allTags);

        if (combinedText.trim().isEmpty()) {
            return Mono.just(generateDefaultTagVector());
        }

        return embeddingService.createEmbedding(combinedText)
                .map(this::formatVector);
    }

    public double calculateCosineSimilarity(String vector1, String vector2) {
        List<Double> v1 = parseVector(vector1);
        List<Double> v2 = parseVector(vector2);

        if (v1.isEmpty() || v2.isEmpty()) {
            return 0.0;
        }

        return cosineSimilarity(v1, v2);
    }

    private double cosineSimilarity(List<Double> v1, List<Double> v2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        int minSize = Math.min(v1.size(), v2.size());

        for (int i = 0; i < minSize; i++) {
            dotProduct += v1.get(i) * v2.get(i);
            norm1 += Math.pow(v1.get(i), 2);
            norm2 += Math.pow(v2.get(i), 2);
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }


    public String calculateAverageVector(List<String> vectorStrings) {
        if (vectorStrings.isEmpty()) {
            return generateDefaultTagVector();
        }

        List<List<Double>> vectors = vectorStrings.stream()
                .map(this::parseVector)
                .filter(vector -> !vector.isEmpty())
                .collect(Collectors.toList());

        if (vectors.isEmpty()) {
            return generateDefaultTagVector();
        }

        List<Double> averageVector = averageVectors(vectors);
        return formatVector(averageVector);
    }

    public String formatVector(List<Double> embedding) {
        return "[" + embedding.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")) + "]";
    }

    public List<Double> parseVector(String vectorString) {
        if (vectorString == null || vectorString.trim().isEmpty()) {
            return List.of();
        }

        try {
            String cleaned = vectorString.replace("[", "").replace("]", "");
            return List.of(cleaned.split(",")).stream()
                    .map(String::trim)
                    .map(Double::parseDouble)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Failed to parse vector string: {}", vectorString);
            return List.of();
        }
    }

    public List<Double> averageVectors(List<List<Double>> vectors) {
        if (vectors.isEmpty()) {
            return List.of();
        }

        int dimensions = vectors.get(0).size();
        List<Double> result = new ArrayList<>(Collections.nCopies(dimensions, 0.0));

        for (List<Double> vector : vectors) {
            for (int i = 0; i < Math.min(dimensions, vector.size()); i++) {
                result.set(i, result.get(i) + vector.get(i));
            }
        }

        return result.stream()
                .map(sum -> sum / vectors.size())
                .collect(Collectors.toList());
    }

    private double normalizeLatitude(double latitude) {
        return Math.max(-1.0, Math.min(1.0, latitude / 90.0));
    }

    private double normalizeLongitude(double longitude) {
        return Math.max(-1.0, Math.min(1.0, longitude / 180.0));
    }

    public String generateDefaultTagVector() {
        List<Double> defaultVector = Collections.nCopies(1536, 0.0);
        return formatVector(defaultVector);
    }

}