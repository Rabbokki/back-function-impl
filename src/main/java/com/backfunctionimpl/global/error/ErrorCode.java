package com.backfunctionimpl.global.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    //USER
    NOT_FOUND_USER(HttpStatus.NOT_FOUND.value(), "U001", "해당 유저를 찾을 수 없습니다."),
    NOT_FOUND_RECOMMENDER(HttpStatus.NOT_FOUND.value(), "U002", "해당 추천인을 찾을 수 없습니다."),
    CANNOT_RECOMMEND_YOURSELF(HttpStatus.BAD_REQUEST.value(), "U003","자신의 닉네임은 적을 수 없습니다."),
    TOKEN_IS_EXPIRED(HttpStatus.BAD_REQUEST.value(), "U004","만료된 액세스 토큰 입니다."),
    REFRESH_TOKEN_IS_EXPIRED(HttpStatus.BAD_REQUEST.value(), "U005","만료된 리프레시 토큰 입니다."),
    DONT_USE_THIS_TOKEN(HttpStatus.BAD_REQUEST.value(), "U006","유효하지 않은 토큰 입니다."),
    NOT_MATCHED_PASSWORD(HttpStatus.BAD_REQUEST.value(), "U006","비밀번호가 틀렸습니다."),
    OVERLAP_CHECK(HttpStatus.BAD_REQUEST.value(), "U007","email이 중복입니다"),
    DELETED_USER_EXCEPTION(HttpStatus.BAD_REQUEST.value(),"U008" ,"이미 탈퇴한 계정입니다.\n다른 계정으로 시도해 주세요." ),

    //CHAT-USER
    NOT_FOUND_USER_IN_CHAT(HttpStatus.NOT_FOUND.value(), "CU001","해당 유저를 찾을 수 없습니다."),

    //POST
    CANNOT_DELETE_NOT_EXIST_POST(HttpStatus.NOT_FOUND.value(), "P001","해당 게시물을 찾을 수 없습니다."),
    CANNOT_FIND_POST_NOT_EXIST(HttpStatus.NOT_FOUND.value(), "P002","해당 게시물을 찾을 수 없거나, 권한이 없습니다"),
    ONLY_CAN_DELETE_POST_WRITER(HttpStatus.FORBIDDEN.value(), "P003","게시글 작성자 만이 게시글을 삭제할 수 있습니다."),
    FAILED_TO_ACCESS_POST(HttpStatus.FORBIDDEN.value(),"P004","게시글 디테일을 읽어 올 수 없습니다"),
//    DOESNT_EXIST_MAIN_POST_FOR_ANONYMOUS(HttpStatus.NOT_FOUND.value(), "P004","남이 쓴 게시글이 존재하지 않습니다."),
//    DOESNT_EXIST_POST_FOR_ANONYMOUS(HttpStatus.NOT_FOUND.value(), "P005","해당하는 게시글이 존재하지 않습니다."),

    //CHATROOM
    NOT_FOUND_ANOTHER_USER(HttpStatus.NOT_FOUND.value(), "R001","상대방을 찾을 수 없습니다."),
    UNKNOWN_CHATROOM(HttpStatus.BAD_REQUEST.value(), "R002","알 수 없는 채팅방 입니다."),
    CANNOT_FOUND_CHATROOM(HttpStatus.NOT_FOUND.value(), "R003","존재하지 않는 채팅방입니다."),
    FORBIDDEN_CHATROOM(HttpStatus.FORBIDDEN.value(), "R004","접근 불가능한 채팅방 입니다."),
    CANNOT_MAKE_ROOM_ALONE(HttpStatus.BAD_REQUEST.value(), "R005","자기자신에게 채팅을 신청할 수 없습니다"),
    DOESNT_EXIST_OTHER_USER(HttpStatus.NOT_FOUND.value(), "R006","채팅상대가 존재하지 않습니다"),
    INVALID_MESSAGE(HttpStatus.NOT_FOUND.value(), "R007","메세지를 확인할 수 없습니다."),
    NotfoundRoom(HttpStatus.NOT_FOUND.value(), "R001", "채팅방이 없습니다."),

    //APPLYMENT
    DOESNT_EXIST_APPLYMENT_FOR_READ(HttpStatus.NOT_FOUND.value(), "A002","해당하는 지원이 존재하지 않습니다"),
    ONLY_CAN_UPDATE_APPLYMENT_WRITER(HttpStatus.FORBIDDEN.value(), "A003","댓글을 작성한 유저만 수정할 수 있습니다."),
    ONLY_CAN_DELETE_APPLYMENT_WRITER(HttpStatus.FORBIDDEN.value(), "A004","댓글을 작성한 유저만 삭제할 수 있습니다."),

    //Notification
    DOESNT_EXIST_NOTIFICATION(HttpStatus.NOT_FOUND.value(), "N001", "해당하는 알림이 존재하지 않습니다")

    ;

    private final int httpStatus;
    private final String code;
    private final String message;
}
