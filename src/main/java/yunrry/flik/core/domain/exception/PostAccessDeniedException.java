package yunrry.flik.core.domain.exception;

import yunrry.flik.core.domain.exception.common.FlikException;

public class PostAccessDeniedException extends FlikException {

    public PostAccessDeniedException(Long userId, Long postId) {
        super(PostErrorCode.POST_ACCESS_DENIED, "CORE");
    }
}