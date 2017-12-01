package org.alfresco.rest.actions;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestActionDefinitionModelsCollection;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;

public class ActionsTests extends RestTest
{
    // TODO: TestGroup.ACTIONS not TestGroup.NODES
    @TestRail(section = { TestGroup.REST_API,TestGroup.NODES }, executionType = ExecutionType.SANITY,
            description = "Verify actions")
    @Test(groups = { TestGroup.REST_API, TestGroup.NODES, TestGroup.SANITY})
    public void testActionDefinitions() throws Exception
    {
        restClient.authenticateUser(dataContent.getAdminUser());

        RestActionDefinitionModelsCollection restActionDefinitions =  restClient.
                withCoreAPI().
                usingActions().
                listActionDefinitions();
        
        restClient.assertStatusCodeIs(HttpStatus.OK);
        assertFalse(restActionDefinitions.isEmpty());
        restActionDefinitions.assertThat().entriesListContains("name", "copy");
        restActionDefinitions.assertThat().entriesListContains("name", "move");
        restActionDefinitions.assertThat().entriesListContains("name", "check-out");
        restActionDefinitions.assertThat().entriesListContains("name", "check-in");
    }

    // TODO: TestGroup.ACTIONS not TestGroup.NODES
    @TestRail(section = { TestGroup.REST_API,TestGroup.NODES }, executionType = ExecutionType.REGRESSION,
            description = "Verify actions error conditions")
    @Test(groups = { TestGroup.REST_API, TestGroup.NODES, TestGroup.REGRESSION})
    public void testActionDefinitionsNegative() throws Exception{
        // Badly formed request -> 400
        {
            restClient.authenticateUser(dataContent.getAdminUser()).
                    // invalid skipCount
                    withParams("skipCount=-1").
                    withCoreAPI().
                    usingActions().
                    listActionDefinitions();
            
            restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST);

        }

        // Unauthorized -> 401
        {

            UserModel userUnauthorized = new UserModel("invalid-user", "invalid-pasword");
            restClient.authenticateUser(userUnauthorized).withCoreAPI().usingActions().listActionDefinitions();
            restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);

        }
    }
}
