📌 1. 기존 구조

예외마다 개별 Exception 클래스를 만들었음

예: UserNotFoundException, ConversationNotFoundException, EmailNotVerifiedException, InvalidRefreshTokenException …

GlobalExceptionHandler 에서 각각 @ExceptionHandler 로 잡아줌

❌ 문제점

예외 클래스가 너무 많아짐

응답 포맷 관리가 분산됨

클라이언트(Android)에서 일관된 에러 처리 불가능

📌 2. RsCode 도입

enum RsCode 를 만들어 상태코드 + 코드 문자열 + 메시지를 중앙집중 관리

예시:

USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "유효하지 않은 리프레시 토큰입니다."),

✅ 장점

모든 에러 코드/메시지가 한 곳에서 관리됨

응답 JSON 구조가 일관됨 (code, message)

📌 3. CustomException 통합

모든 비즈니스 예외는 이제 다음처럼 던짐:

throw new CustomException(RsCode.XXX);


GlobalExceptionHandler 는 딱 하나의 @ExceptionHandler(CustomException.class) 만 두고, 여기서 RsCode 값을 읽어 클라이언트에 내려줌

그 외 스프링/인증/검증 관련 기본 예외(MethodArgumentNotValidException, AuthenticationException 등)만 별도로 처리

📌 4. 서비스 코드 리팩터링
ConversationService

UserNotFoundException → throw new CustomException(RsCode.USER_NOT_FOUND)

ConversationNotFoundException → throw new CustomException(RsCode.CHATROOM_NOT_FOUND)

ConversationForbiddenException → throw new CustomException(RsCode.FORBIDDEN)

ChatMessageService

AiEmptyResponseException → throw new CustomException(RsCode.AI_EMPTY_RESPONSE)

Auth 관련

EmailNotVerifiedException → throw new CustomException(RsCode.EMAIL_VERIFICATION_FAILED)

InvalidRefreshTokenException → throw new CustomException(RsCode.INVALID_REFRESH_TOKEN)

📌 5. 일관된 응답 포맷

ErrorResponse DTO 예시:

{
  "code": "USER_NOT_FOUND",
  "message": "사용자를 찾을 수 없습니다."
}


Android, Web 프론트 등 클라이언트는 code 값 기준으로 분기 처리 가능

✅ 이렇게 해서 예외 처리가 CustomException + RsCode + GlobalExceptionHandler 구조로 단순화되었고, 응답이 일관적으로 내려가도록 리팩터링 완료했습니다.
