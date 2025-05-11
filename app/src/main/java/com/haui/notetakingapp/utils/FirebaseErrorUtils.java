package com.haui.notetakingapp.utils;

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class FirebaseErrorUtils {
    public static String getLocalizedErrorMessage(Exception exception) {
        if (exception == null) {
            return "Đã xảy ra lỗi không xác định";
        }

        String errorMessage = exception.getMessage();

        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            return "Email hoặc mật khẩu không chính xác";
        } else if (exception instanceof FirebaseAuthInvalidUserException) {
            return "Tài khoản không tồn tại hoặc đã bị vô hiệu hóa";
        } else if (exception instanceof FirebaseAuthUserCollisionException) {
            return "Email này đã được sử dụng bởi một tài khoản khác";
        }

        if (errorMessage != null) {
            if (errorMessage.contains("ERROR_INVALID_EMAIL")) {
                return "Địa chỉ email không hợp lệ";
            } else if (errorMessage.contains("ERROR_WRONG_PASSWORD")) {
                return "Mật khẩu không chính xác";
            } else if (errorMessage.contains("ERROR_USER_NOT_FOUND")) {
                return "Không tìm thấy tài khoản với email này";
            } else if (errorMessage.contains("ERROR_USER_DISABLED")) {
                return "Tài khoản này đã bị vô hiệu hóa";
            } else if (errorMessage.contains("ERROR_TOO_MANY_REQUESTS")) {
                return "Quá nhiều yêu cầu, vui lòng thử lại sau";
            } else if (errorMessage.contains("ERROR_OPERATION_NOT_ALLOWED")) {
                return "Thao tác không được cho phép";
            } else if (errorMessage.contains("ERROR_WEAK_PASSWORD")) {
                return "Mật khẩu quá yếu, vui lòng sử dụng mật khẩu mạnh hơn";
            } else if (errorMessage.contains("ERROR_INVALID_CREDENTIAL")) {
                return "Thông tin xác thực không hợp lệ";
            } else if (errorMessage.contains("ERROR_EMAIL_ALREADY_IN_USE")) {
                return "Email này đã được sử dụng bởi một tài khoản khác";
            } else if (errorMessage.contains("ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL")) {
                return "Tài khoản đã tồn tại với thông tin xác thực khác";
            } else if (errorMessage.contains("ERROR_CREDENTIAL_ALREADY_IN_USE")) {
                return "Thông tin xác thực này đã được liên kết với một tài khoản khác";
            } else if (errorMessage.contains("ERROR_REQUIRES_RECENT_LOGIN")) {
                return "Thao tác này yêu cầu đăng nhập lại gần đây";
            } else if (errorMessage.contains("ERROR_NETWORK")) {
                return "Lỗi kết nối mạng, vui lòng kiểm tra lại kết nối internet";
            } else if (errorMessage.contains("ERROR_INTERNAL_ERROR")) {
                return "Lỗi nội bộ của hệ thống Firebase";
            }
        }

        return "Đã xảy ra lỗi: " + (errorMessage != null ? errorMessage : "không xác định");
    }
}
