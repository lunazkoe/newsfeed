package com.lunazkoe.newsfeed.domain.interest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "수정할 관심사 정보")
public record InterestUpdateRequest(
    @Schema(description = "수정 키워드 목록")
    @NotNull(message = "키워드 목록은 필수입니다.")
    @Size(min = 1, max = 10, message = "키워드는 1개 이상 10개 이하로 등록 가능합니다.")
    List<@NotBlank(message="키워드는 공백일 수 없습니다.") @Size(max=20, message="키워드는 20자를 넘을 수 없습니다.") String> keywords
) {

}
