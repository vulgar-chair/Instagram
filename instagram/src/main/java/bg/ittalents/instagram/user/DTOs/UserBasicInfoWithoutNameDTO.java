package bg.ittalents.instagram.user.DTOs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserBasicInfoWithoutNameDTO {

    private Long id;
    private String username;
    private String profilePictureUrl;
}
