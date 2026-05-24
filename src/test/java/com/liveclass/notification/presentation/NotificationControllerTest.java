package com.liveclass.notification.presentation;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.liveclass.notification.application.NotificationApplicationService;
import com.liveclass.notification.domain.NotificationChannel;
import com.liveclass.notification.domain.NotificationType;
import com.liveclass.notification.presentation.dto.NotificationSendRequest;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(NotificationController.class)
@AutoConfigureRestDocs
@DisplayNameGeneration(ReplaceUnderscores.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationApplicationService notificationApplicationService;

    @Test
    void 알림_발송_요청_API는_202_Accepted를_반환한다() throws Exception {
        // given
        NotificationSendRequest request = new NotificationSendRequest(
            "ev1", "user1", NotificationType.PAYMENT_SUCCESS, NotificationChannel.EMAIL, "payload"
        );

        // when & then
        mockMvc.perform(post("/api/v1/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isAccepted())
            .andDo(document("notification-send",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint())
            ));
    }

    @Test
    void 알림_읽음_처리_API는_200_OK를_반환한다() throws Exception {
        // given
        Long notificationId = 1L;

        // when & then
        mockMvc.perform(patch("/api/v1/notifications/{notificationId}/read", notificationId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(document("notification-read",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint())
            ));

        verify(notificationApplicationService).readNotification(notificationId);
    }

    @Test
    void 동시_읽음_처리_요청_시_충돌이_발생하면_409_Conflict를_반환한다() throws Exception {
        // given
        Long notificationId = 1L;
        doThrow(new OptimisticLockingFailureException("충돌 발생"))
            .when(notificationApplicationService).readNotification(notificationId);

        // when & then
        mockMvc.perform(patch("/api/v1/notifications/{notificationId}/read", notificationId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict())
            .andDo(document("notification-read-conflict",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint())
            ));
    }
}
