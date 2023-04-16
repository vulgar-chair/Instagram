package bg.ittalents.instagram.user.DTOs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserWithoutPassAndEmailDTO {
    private Long id;
    private String username;
    private String name;
    private String bio;
    private String profilePictureUrl;
}
