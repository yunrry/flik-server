package yunrry.flik.adapters.in.dto;

import java.util.List;


public record UserPostIdsReponse(
       List<Long> ids
) {
    public static UserPostIdsReponse from(List<Long> ids){
        return new UserPostIdsReponse(ids);
    }
}