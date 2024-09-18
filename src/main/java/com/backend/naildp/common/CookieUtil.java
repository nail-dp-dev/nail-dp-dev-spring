package com.backend.naildp.common;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.backend.naildp.dto.auth.SocialUserInfoDto;
import com.google.gson.Gson;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CookieUtil {
	static Gson gson = new Gson();

	public void setUserInfoCookie(HttpServletResponse response, SocialUserInfoDto userInfo) {
		String userInfoJson = gson.toJson(userInfo);

		try {
			// JSON 문자열을 URL 인코딩
			String encodedUserInfoJson = URLEncoder.encode(userInfoJson, "UTF-8");

			Cookie cookie = new Cookie("userInfo", encodedUserInfoJson);
			cookie.setMaxAge(1800); // 30 minutes
			cookie.setHttpOnly(true);
			cookie.setPath("/");

			response.addCookie(cookie);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public SocialUserInfoDto getUserInfoFromCookie(HttpServletRequest req) {
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("userInfo".equals(cookie.getName())) {
					try {
						// 쿠키 값을 URL 디코딩
						String decodedUserInfoJson = URLDecoder.decode(cookie.getValue(), "UTF-8");
						Gson gson = new Gson();
						log.info(decodedUserInfoJson);
						return gson.fromJson(decodedUserInfoJson, SocialUserInfoDto.class);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
			}
		}
		throw new NullPointerException("쿠키가 존재하지 않습니다.");
	}

	public static void deleteCookie(String cookieName, HttpServletRequest req, HttpServletResponse res) {
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(cookieName)) {
					cookie.setMaxAge(0);
					cookie.setHttpOnly(true);
					cookie.setPath("/");

					res.addCookie(cookie);
				}
			}

		}
	}

	public static Optional<Cookie> getStateCookie(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(name)) {
					return Optional.of(cookie);
				}
			}
		}
		return Optional.empty();
	}

	public static void addStateCookie(HttpServletResponse response, String name, String value, int maxAge,
		boolean httpOnly, boolean secure) {
		Cookie cookie = new Cookie(name, value);
		cookie.setPath("/");
		cookie.setHttpOnly(httpOnly);
		// cookie.setSecure(secure);
		cookie.setMaxAge(maxAge);
		// cookie.setameSite(sameSite);
		response.addCookie(cookie);
	}

	public static String serialize(Object object) {
		String jsonString = gson.toJson(object);
		return Base64.getUrlEncoder().encodeToString(jsonString.getBytes());
	}

	public static <T> T deserialize(Cookie cookie, Class<T> cls) {
		String decodedValue = new String(Base64.getUrlDecoder().decode(cookie.getValue()));
		return gson.fromJson(decodedValue, cls);
	}
}
