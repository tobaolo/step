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
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.oauth.OAuthService;
import com.google.appengine.api.oauth.OAuthServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.oauth.OAuthServiceFailureException;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/user")
public class LoginServlet extends HttpServlet {
  
  @Override 
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Set response type.
    response.setContentType("text/html");
    
    UserService userService = UserServiceFactory.getUserService();

    // If user is logged in, get user credentials and create logout link.
    if (userService.isUserLoggedIn()) {
      User user = userService.getCurrentUser();
      String userEmail = user.getEmail();
      String logoutURL = userService.createLogoutURL("/user");
      
      response.getWriter().println("Email: " + userEmail);
      response.getWriter().println("Logout  <a href=\"" + logoutURL + "\">here</a>");

    // If user is not logged in, show log in info.
    } else {
      String loginURL = userService.createLoginURL("/user");
      
      response.getWriter().println("Login  <a href=\"" + loginURL + "\">here</a>");
    }
  }
}