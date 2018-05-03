/*
 * Copyright (C) 2017 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.rest.search;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestRequestRangesModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Faceted search test.
 * @author Gethin James
 *
 */
public class PivotFacetedSearchTest extends AbstractSearchTest
{

    @Test(groups = { TestGroup.REST_API, TestGroup.SEARCH, TestGroup.ASS_1 })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SEARCH, TestGroup.ASS_1 },
              executionType = ExecutionType.REGRESSION,
              description = "Checks errors with pivot using Search api")
    public void searchWithPivotingErrors() throws Exception
    {
        SearchRequest query = carsQuery();

        RestRequestFacetFieldsModel facetFields = new RestRequestFacetFieldsModel();
        List<RestRequestFacetFieldModel> list = new ArrayList<>();
        list.add(new RestRequestFacetFieldModel("'creator'"));
        facetFields.setFacets(list);
        query.setFacetFields(facetFields);
        query.setIncludeRequest(false);
        List<RestRequestPivotModel> pivotModelList = new ArrayList<>();
        RestRequestPivotModel pivots = new RestRequestPivotModel();
        pivotModelList.add(pivots);
        query.setPivots(pivotModelList);

        query(query);
        
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                    .containsSummary(String.format(RestErrorModel.MANDATORY_PARAM, "pivot key"));

        pivots.setKey("none_like_this");
        
        query(query);
        
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                  .assertLastError().containsSummary("invalid argument was received")
                    .containsSummary("Pivot parameter none_like_this does not reference");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SEARCH, TestGroup.ASS_1 })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SEARCH, TestGroup.ASS_1 },
              executionType = ExecutionType.REGRESSION,
              description = "Checks with pivot using Search api")
    public void searchWithPivoting() throws Exception
    {
        SearchRequest query = carsQuery();

        RestRequestFacetFieldsModel facetFields = new RestRequestFacetFieldsModel();
        List<RestRequestFacetFieldModel> list = new ArrayList<>();
        list.add(new RestRequestFacetFieldModel("creator"));
        facetFields.setFacets(list);
        query.setFacetFields(facetFields);
        query.setIncludeRequest(false);

        SearchResponse response =  query(query);
        response.getContext().assertThat().field("facetsFields").isNotNull();

        List<RestRequestPivotModel> pivotModelList = new ArrayList<>();
        RestRequestPivotModel pivots = new RestRequestPivotModel();
        pivots.setKey("creator");
        pivotModelList.add(pivots);
        query.setPivots(pivotModelList);
        response =  query(query);

        //Pivot key has matched facet field so there is no longer a facet fields response
        assertPivotResponse(response, "creator", null);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SEARCH, TestGroup.ASS_1 })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SEARCH, TestGroup.ASS_1 },
              executionType = ExecutionType.REGRESSION,
              description = "Checks nested pivot using Search api")
    public void searchWithNestedPivoting() throws Exception
    {
        SearchRequest query = carsQuery();

        RestRequestFacetFieldsModel facetFields = new RestRequestFacetFieldsModel();
        List<RestRequestFacetFieldModel> list = new ArrayList<>();
        RestRequestFacetFieldModel restRequestFacetFieldModel = new RestRequestFacetFieldModel("SITE");
        restRequestFacetFieldModel.setLabel("site");
        list.add(restRequestFacetFieldModel);
        list.add(new RestRequestFacetFieldModel("creator"));
        list.add(new RestRequestFacetFieldModel("modifier"));
        facetFields.setFacets(list);
        query.setFacetFields(facetFields);
        query.setIncludeRequest(false);

        List<RestRequestPivotModel> pivotModelList = new ArrayList<>();
        RestRequestPivotModel sitepivots = new RestRequestPivotModel();
        sitepivots.setKey("site");
        RestRequestPivotModel creatorpivot = new RestRequestPivotModel();
        creatorpivot.setKey("creator");
        RestRequestPivotModel modifierpivot = new RestRequestPivotModel();
        modifierpivot.setKey("modifier");
        sitepivots.setPivots(Arrays.asList(creatorpivot, modifierpivot));
        pivotModelList.add(sitepivots);
        query.setPivots(pivotModelList);

        SearchResponse response =  query(query);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                    .assertLastError().containsSummary("invalid argument was received")
                    .containsSummary("Currently only 1 nested pivot is supported, you have 2");

        pivotModelList = new ArrayList<>();
        sitepivots = new RestRequestPivotModel();
        sitepivots.setKey("site");
        sitepivots.setPivots(Arrays.asList(modifierpivot));
        pivotModelList.add(sitepivots);
        pivotModelList.add(creatorpivot);
        query.setPivots(pivotModelList);

        response =  query(query);
        assertPivotResponse(response, "SITE", "site");

        RestGenericFacetResponseModel siteResponse = response.getContext().getFacets().get(0);
        RestGenericBucketModel bucket = siteResponse.getBuckets().get(0);
        RestGenericFacetResponseModel modifiedResponse = bucket.getFacets().get(0);
        modifiedResponse.assertThat().field("type").is("pivot");
        modifiedResponse.assertThat().field("label").is("modifier");

        RestGenericFacetResponseModel creatorResponse = response.getContext().getFacets().get(1);
        creatorResponse.assertThat().field("type").is("pivot");
        creatorResponse.assertThat().field("label").is("creator");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SEARCH, TestGroup.ASS_1 })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SEARCH, TestGroup.ASS_1 },
              executionType = ExecutionType.REGRESSION,
              description = "Checks range pivots using Search api")
    public void searchWithRangePivoting() throws Exception
    {
        SearchRequest query = carsQuery();

        Pagination pagination = new Pagination();
        pagination.setMaxItems(2);
        query.setPaging(pagination);
        RestRequestFacetFieldsModel facetFields = new RestRequestFacetFieldsModel();
        List<RestRequestFacetFieldModel> list = new ArrayList<>();
        list.add(new RestRequestFacetFieldModel("creator"));
        facetFields.setFacets(list);
        query.setFacetFields(facetFields);
        query.setIncludeRequest(false);

        RestRequestRangesModel facetRangeModel = new RestRequestRangesModel();
        facetRangeModel.setField("created");
        facetRangeModel.setStart("2015-09-29T10:45:15.729Z");
        facetRangeModel.setEnd("2016-09-29T10:45:15.729Z");
        facetRangeModel.setGap("+280DAY");
        facetRangeModel.setLabel("aRange");
        List<RestRequestRangesModel> ranges = new ArrayList<RestRequestRangesModel>();
        ranges.add(facetRangeModel);
        query.setRanges(ranges);

        List<RestRequestPivotModel> pivotModelList = new ArrayList<>();
        RestRequestPivotModel creatorpivot = new RestRequestPivotModel();
        creatorpivot.setKey("creator");
        RestRequestPivotModel rangepivot = new RestRequestPivotModel();
        rangepivot.setKey("aRange");
        creatorpivot.setPivots(Arrays.asList(rangepivot));
        pivotModelList.add(creatorpivot);
        query.setPivots(pivotModelList);

        SearchResponse response =  query(query);
        RestGenericFacetResponseModel facetResponseModel = response.getContext().getFacets().get(1);
        facetResponseModel.assertThat().field("type").is("pivot");
        facetResponseModel.assertThat().field("label").is("creator");
        RestGenericBucketModel bucket = facetResponseModel.getBuckets().get(0);
        RestGenericFacetResponseModel rangeResponse = bucket.getFacets().get(0);
        rangeResponse.assertThat().field("type").is("range");
        assertTrue(rangeResponse.getBuckets().size()>0);

    }

    private void assertPivotResponse(SearchResponse response, String field, String alabel) throws Exception
    {
        String label = alabel!=null?alabel:field;
        response.getContext().assertThat().field("facetsFields").isNull();
        response.getContext().assertThat().field("facets").isNotEmpty();
        RestGenericFacetResponseModel facetResponseModel = response.getContext().getFacets().get(0);
        facetResponseModel.assertThat().field("type").is("pivot");
        facetResponseModel.assertThat().field("label").is(label);
        RestGenericBucketModel bucket = facetResponseModel.getBuckets().get(0);
        bucket.assertThat().field("label").isNotEmpty();
        bucket.assertThat().field("filterQuery").is(field+":\""+bucket.getLabel()+"\"");
        Assert.assertEquals("count", bucket.getMetrics().get(0).getType());
        Assert.assertTrue(bucket.getMetrics().get(0).getValue().toString().contains("{count="));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SEARCH, TestGroup.ASS_1 })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SEARCH, TestGroup.ASS_1 },
              executionType = ExecutionType.REGRESSION,
              description = "Checks with pivot using Search api and a label as a key")
    public void searchWithPivotingUsingLabel() throws Exception
    {
        SearchRequest query = carsQuery();
        RestRequestFacetFieldsModel facetFields = new RestRequestFacetFieldsModel();
        List<RestRequestFacetFieldModel> list = new ArrayList<>();
        RestRequestFacetFieldModel creatorFacetFieldModel = new RestRequestFacetFieldModel("creator");
        creatorFacetFieldModel.setLabel("create");
        list.add(creatorFacetFieldModel);
        RestRequestFacetFieldModel restRequestFacetFieldModel = new RestRequestFacetFieldModel("modifier");
        restRequestFacetFieldModel.setLabel("aLabel");
        list.add(restRequestFacetFieldModel);
        facetFields.setFacets(list);
        query.setFacetFields(facetFields);
        query.setIncludeRequest(false);
        RestRequestPivotModel pivots = new RestRequestPivotModel();
        pivots.setKey("create");
        RestRequestPivotModel pivotmod = new RestRequestPivotModel();
        pivotmod.setKey("aLabel");

        List<RestRequestPivotModel> pivotModelList = new ArrayList<>();
        pivotModelList.add(pivots);
        pivotModelList.add(pivotmod);
        query.setPivots(pivotModelList);
        SearchResponse response =  query(query);
        assertPivotResponse(response, "creator", "create");

        //Now check the nesting
        RestGenericFacetResponseModel labelResponseModel = response.getContext().getFacets().get(1);
        labelResponseModel.assertThat().field("type").is("pivot");
        labelResponseModel.assertThat().field("label").isNotEmpty();
        labelResponseModel.assertThat().field("label").is("aLabel");
        RestGenericBucketModel bucket = labelResponseModel.getBuckets().get(0);
        bucket.assertThat().field("filterQuery").is("modifier:\""+bucket.getLabel()+"\"");
        Assert.assertEquals("count", bucket.getMetrics().get(0).getType());
        Assert.assertTrue(bucket.getMetrics().get(0).getValue().toString().contains("{count="));
    }
}
