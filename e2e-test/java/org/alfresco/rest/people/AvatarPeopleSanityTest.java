package org.alfresco.rest.people;

import java.io.File;

import org.alfresco.rest.RestTest;
import org.alfresco.utility.Utility;
import org.alfresco.utility.exception.DataPreparationException;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AvatarPeopleSanityTest extends RestTest {

    private UserModel userModel;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws DataPreparationException
    {
        userModel = dataUser.createRandomTestUser();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API,
            TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Update Avatar for People")
    public void updateGetAvatarForPeople() throws Exception
    {

        File avatarFile = Utility.getResourceTestDataFile("avatar.jpg");
        restClient.authenticateUser(userModel);
        restClient.withCoreAPI().usingAuthUser().uploadAvatarContent(restProperties.envProperty().getFullServerUrl(), avatarFile).statusCode(200);
        restClient.authenticateUser(userModel).withCoreAPI().usingAuthUser().downloadAvatarContent();
        restClient.assertStatusCodeIs(HttpStatus.OK);

    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API,
            TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Update Avatar for People")
    public void removeGetAvatarForPeople() throws Exception
    {
        File avatarFile = Utility.getResourceTestDataFile("avatar.jpg");
        restClient.authenticateUser(userModel);
        restClient.withCoreAPI().usingAuthUser().uploadAvatarContent(restProperties.envProperty().getFullServerUrl(), avatarFile).statusCode(200);

        restClient.authenticateUser(userModel);
        restClient.withCoreAPI().usingAuthUser().resetAvatarImageRequest();
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        restClient.authenticateUser(userModel).withCoreAPI().usingAuthUser().downloadAvatarContent();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND);
    }

}
