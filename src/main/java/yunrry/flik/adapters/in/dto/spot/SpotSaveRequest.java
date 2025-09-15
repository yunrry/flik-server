package yunrry.flik.adapters.in.dto.spot;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SpotSaveRequest {

    @NotNull(message = "장소 ID는 필수입니다")
    private Long spotId;
}
