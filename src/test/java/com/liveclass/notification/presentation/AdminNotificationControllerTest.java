package com.liveclass.notification.presentation;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.liveclass.notification.application.AdminNotificationService;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminNotificationController.class)
@AutoConfigureRestDocs
@DisplayNameGeneration(ReplaceUnderscores.class)
class AdminNotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminNotificationService adminNotificationService;

    @Test
    void 실패한_알림_목록을_조회한다() throws Exception {
        // given
        when(adminNotificationService.getFailedNotifications()).thenReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/v1/admin/notifications/failed"))
            .andExpect(status().isOk())
            .andDo(document("admin-notifications-failed",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint())
            ));
    }

    @Test
    void 특정_알림을_수동으로_재시도한다() throws Exception {
        // given
        Long eventId = 1L;

        // when & then
        mockMvc.perform(post("/api/v1/admin/notifications/retry/{eventId}", eventId))
            .andExpect(status().isOk())
            .andDo(document("admin-notifications-retry",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint())
            ));

        verify(adminNotificationService).retryFailedNotification(eventId);
    }
}
