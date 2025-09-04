package yunrry.flik.core.domain.exception;

import yunrry.flik.core.domain.exception.common.FlikException;

public class PostNotFoundException extends FlikException {

    public PostNotFoundException(Long postId) {
        super(PostErrorCode.POST_NOT_FOUND, "CORE");
    }
}