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
import com.google.gson.Gson;
import java.io.IOException;
import java.util.*;
import java.util.Arrays; 
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns wordtree content. */
@WebServlet("/wordtree")
public class WordTreeServlet extends HttpServlet {
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("WordTreeSentence");

    List<Entity> sentenceEntityList = datastore.prepare(query)
        .asList(FetchOptions.Builder.withDefaults());
    
     // Get list of text from comment entities.
    ArrayList<String[]> sentenceList = new ArrayList<String[]>();
    for (Entity sentence : sentenceEntityList) {
      String text = (String) sentence.getProperty("text");
      String fullText = "Black " + text.trim();
      String textArray[] = new String[] { fullText };
      sentenceList.add(textArray);
    }

    // Convert comments to JSON and send as the response.
    Gson gson = new Gson();

    response.setContentType("application/json");
    response.getWriter().println(gson.toJson(sentenceList));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get sentence from form.
    String text = request.getParameter("sentence-text");

    // Store sentence as entities in database.
    Entity wordTreeSentenceEntity = new Entity("WordTreeSentence");
    wordTreeSentenceEntity.setProperty("text", text);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(wordTreeSentenceEntity);  

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }
}
