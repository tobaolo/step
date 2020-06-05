// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.FetchOptions.Builder;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.Arrays; 
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/** Servlet that returns comment content. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Retreive comment entities from database based on comment limit.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("Comment").addSort("timestamp", SortDirection.ASCENDING);

    int commentLimit = getCommentLimit(request);
    List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(commentLimit));

    // Get list of text from comment entities.
    List<String> comments = new ArrayList<>();
    for (Entity comment : results) {
      comments.add((String) comment.getProperty("text"));
    }

    // Convert comments to JSON and send as the response.
    Gson gson = new Gson();

    response.setContentType("application/json");
    response.getWriter().println(gson.toJson(comments));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get comment from form.
    String text = request.getParameter("text-input");
    long timestamp = System.currentTimeMillis();

    // Store comments as entities in database.
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("text", text);
    commentEntity.setProperty("timestamp", timestamp);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);  

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }

  /** Returns the comment limit entered from HTTP request param. */
  private int getCommentLimit(HttpServletRequest request) {
    // Get the input from the form.
    String commentLimitString = request.getParameter("limit-comments");

    // Convert the input to an int.
    int commentLimit = Integer.parseInt(commentLimitString);

    return commentLimit;
  }
}
