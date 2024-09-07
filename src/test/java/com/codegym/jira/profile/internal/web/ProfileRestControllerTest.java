package com.codegym.jira.profile.internal.web;

import com.codegym.jira.AbstractControllerTest;
import com.codegym.jira.common.BaseHandler;
import com.codegym.jira.login.AuthUser;
import com.codegym.jira.login.Role;
import com.codegym.jira.login.User;
import com.codegym.jira.mail.MailService;
import com.codegym.jira.profile.ProfileTo;
import com.codegym.jira.profile.internal.ProfileMapper;
import com.codegym.jira.profile.internal.ProfileRepository;
import com.codegym.jira.profile.internal.model.Profile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class ProfileRestControllerTest extends AbstractControllerTest {
    private static final String REST_URL_PROJECT = BaseHandler.REST_URL + "/profile";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProfileRepository profileRepository;

    @MockBean
    private ProfileMapper profileMapper;

    @MockBean
    private MailService mailService;

    @Test
    void testGetProfile() throws Exception {
        // Given
        User mockUser = new User(1L, "testUser@example.com", "password", "Test", "User", "TUser", Role.MANAGER);
        AuthUser authUser = new AuthUser(mockUser);

        Profile profile = new Profile(1L);
        profile.setLastLogin(LocalDateTime.of(2023, 9, 6, 12, 30));
        profile.setMailNotifications(3L);

        Set<String> notifications = Set.of("notification1");
        ProfileTo profileTo = new ProfileTo(1L, notifications, null);
        profileTo.setLastLogin(LocalDateTime.of(2023, 9, 6, 12, 30));

        when(profileRepository.getOrCreate(anyLong())).thenReturn(profile);
        when(profileMapper.toTo(profile)).thenReturn(profileTo);

        // When
        mockMvc.perform(get(REST_URL_PROJECT)
                        .with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.lastLogin").value("2023-09-06T12:30:00"))
                .andExpect(jsonPath("$.mailNotifications").value("notification1"));

        // Then
        verify(profileRepository).getOrCreate(mockUser.getId());
        verify(profileMapper).toTo(profile);
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"MANAGER"})
    void testUpdateProfile() throws Exception {
        // Given
        User mockUser = new User(1L, "testUser@example.com", "password", "Test", "User", "TUser", Role.MANAGER);
        AuthUser authUser = new AuthUser(mockUser);

        String profileToJson = "{"
                + "\"id\": 1,"
                + "\"lastLogin\": \"2023-09-06T12:30:00\","
                + "\"mailNotifications\": null,"
                + "\"contacts\": null"
                + "}";

        Profile profile = new Profile(1L);
        profile.setLastLogin(LocalDateTime.of(2023, 9, 6, 12, 30));
        profile.setMailNotifications(3L);

        when(profileRepository.getOrCreate(anyLong())).thenReturn(profile);
        when(profileMapper.updateFromTo(any(Profile.class), any(ProfileTo.class))).thenReturn(profile);

        // When
        mockMvc.perform(put(ProfileRestController.REST_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileToJson) // Usa el JSON String aquí
                        .with(user(authUser)))
                .andExpect(status().isNoContent());

        // Then
        verify(profileRepository).getOrCreate(mockUser.getId());
        verify(profileMapper).updateFromTo(any(Profile.class), any(ProfileTo.class));
        verify(profileRepository).save(profile);
    }


}