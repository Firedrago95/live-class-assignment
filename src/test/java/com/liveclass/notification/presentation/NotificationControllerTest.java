package com.liveclass.notification.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.liveclass.notification.application.NotificationApplicationService;
import com.liveclass.notification.domain.NotificationChannel;
import com.liveclass.notification.domain.NotificationType;
import com.liveclass.notification.presentation.dto.NotificationSendRequest;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(NotificationController.class)
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
                .andExpect(status().isAccepted());
    }
}
