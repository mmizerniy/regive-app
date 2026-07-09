package mmdev.regiveapp.auth;

public record LoginResponse(
        String token,String tokenType
) {
    public LoginResponse(String token){
        this(token,"Bearer");
    }
}
