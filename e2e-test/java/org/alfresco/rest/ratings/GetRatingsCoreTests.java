package org.alfresco.rest.ratings;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestRatingModelsCollection;
import org.alfresco.utility.exception.DataPreparationException;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GetRatingsCoreTests extends RestTest
{
    private SiteModel siteModel;
    private UserModel adminUserModel, userModel;
    private FileModel document;
    private RestRatingModelsCollection ratingsModel;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws DataPreparationException
    {
        adminUserModel = dataUser.getAdminUser();
        userModel = dataUser.createRandomTestUser();
        siteModel = dataSite.usingAdmin().createPublicRandomSite();
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws DataPreparationException, Exception
    {
        document = dataContent.usingSite(siteModel).usingAdmin().createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        restClient.authenticateUser(userModel).withCoreAPI().usingNode(document).rateStarsToDocument(5);
        restClient.authenticateUser(userModel).withCoreAPI().usingNode(document).likeDocument();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION, 
            description = "Check that rating for invalid maxItems status code is 400")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.CORE })
    public void checkInvalidMaxItemsStatusCode() throws Exception
    {

        restClient.authenticateUser(adminUserModel).withParams("maxItems=0").withCoreAPI().usingResource(document).getRatings();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary("Only positive values supported for maxItems");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION, 
            description = "Check that rating for invalid skipCount status code is 400")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.CORE })
    public void checkInvalidSkipCountStatusCode() throws Exception
    {

        restClient.authenticateUser(adminUserModel).withParams("skipCount=AB").withCoreAPI().usingResource(document).getRatings();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary("Invalid paging parameter skipCount:AB");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION, 
            description = "If nodeId does not exist status code is 404 when a document is liked")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.CORE })
    public void addLikeUsingInvalidNodeId() throws Exception
    {
        FileModel document = dataContent.usingSite(siteModel).usingAdmin().createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        restClient.authenticateUser(userModel).withCoreAPI().usingNode(document).rateStarsToDocument(5);
        restClient.authenticateUser(userModel).withCoreAPI().usingNode(document).likeDocument();
        document.setNodeRef(RandomStringUtils.randomAlphanumeric(20));
        
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(document).getRatings();

        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, document.getNodeRef()));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION, 
            description = "Check that rating value is TRUE for a like rating")  
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.CORE })
    public void checkRatingValueIsTrueForLikedDoc() throws Exception
    {

        ratingsModel = restClient.authenticateUser(userModel).withCoreAPI().usingResource(document).getRatings();
        restClient.assertStatusCodeIs(HttpStatus.OK);

        ratingsModel.assertThat().entriesListContains("myRating", "true")
            .assertThat().entriesListContains("id", "likes");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION, 
            description = "Check that rating value is an INTEGER value for stars rating")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.CORE })
    public void checkRatingValueIsIntegerForStarsRating() throws Exception
    {

        ratingsModel = restClient.authenticateUser(userModel).withCoreAPI().usingResource(document).getRatings();
        restClient.assertStatusCodeIs(HttpStatus.OK);

        ratingsModel.assertThat().entriesListContains("myRating", "5")
            .assertThat().entriesListContains("id", "fiveStar");
    }

}
