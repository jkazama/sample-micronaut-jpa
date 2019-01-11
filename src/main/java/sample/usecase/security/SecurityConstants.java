package sample.usecase.security;

public interface SecurityConstants {
    
    String KeyAuth = "extension.auth";
    String KeyAdmin = KeyAuth + ".admin";
    String KeyDummyUsername = KeyAuth + ".dummy-username";
    String KeyDummyUsernameEL = "${" + KeyDummyUsername + ":}";

}
