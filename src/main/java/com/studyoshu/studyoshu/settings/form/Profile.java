package com.studyoshu.studyoshu.settings.form;

import com.studyoshu.studyoshu.domain.Account;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
public class Profile {
    @Length (max = 35)
    private String bio;
    @Length (max = 200)
    private String url;
    @Length (max = 50)
    private String occupation;
    @Length (max = 50)
    private String location;

    private String profileImage;

//    public Profile(Account account) {
//        this.bio = account.getBio();
//        this.url = account.getUrl();
//        this.occupation = account.getOccupation();
//        this.location = account.getLocation();
//        this.profileImage = account.getProfileImage();
//    }
}
