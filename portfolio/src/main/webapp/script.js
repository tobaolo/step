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

/**
 * Fetches comments and appends them as list elements.
 */
function getComments() {
  
  fetch('/user').then((response) => response.text()).then((loginStatus) => {

    loginObj = JSON.parse(loginStatus);
    const commentLimit = document.getElementById('limit-comments').value;

    // Get the comments from the database.
    fetch('/data?limit-comments=' + commentLimit)
        .then(response => response.text())
        .then((comments) => {
      const commentGrid = document.getElementById('comments-grid');
      const commentsObj = JSON.parse(comments);
      commentGrid.innerHTML = '';
      commentsObj.forEach((comment) => {
        const li = document.createElement('li');
        li.innerText = Object.keys(comment)[0] + ': ' 
            + Object.values(comment)[0];
        li.classList.add('list-group-item');
        commentGrid.appendChild(li);
        });
      });

    // Determine if user is logged in and display comment submission if logged.
    if (loginObj.isLoggedIn) {
      document.getElementById('comment-form').style.display = 'block';
      const logoutLine = document.createElement('p');
      logoutLine.innerHTML = 'Logout <a href=\"' + loginObj.logoutURL + 
          '">Here</a>';
      document.getElementById('comments').appendChild(logoutLine);
    } else {
      const loginLine = document.createElement('p');
      loginLine.innerHTML = 'Sign in <a href=\"' + loginObj.loginURL + 
          '">Here</a> to leave a comment';
      document.getElementById('comments').appendChild(loginLine);
    }
  });
}

/**
 * Post request to delete comments.
 */
function deleteComments() {
  fetch('/delete-data', {method: 'post'}).then(getComments());
}

/**
 * Validates whether text has been inputted before submitting.
 */
function validateText() {
  const comment = document.getElementById('comment-box').value.trim();
  if (!comment) {
    alert('Cannot submit an empty comment.');
    return false;
  }
}

/**
 * Adds a random quote to the page.
 */
function addRandomQuote() {
  const quotes =
      ['Favorite Quote: I\'m not hiding anything that\'s hidden or \
      found, or finding anything that\'s found or hidden. I\'m living \
      my best life. -Jamal, On My Block', 'They\'re relentless! -The \
      Grinch', 'Favorite Movie: The Grinch', 'Favorite Movie: Inception', 
      'Favorite TV Show: Avatar The Last Airbender', 'Favortie Artist:  \
      Kizz Daniel', 'Favorite Spotify Playlist: Mellow Bars',
      'Favorite Programming Language: Python', 'Favorite Sport: Soccer',
      'Favorite Food: Mama\'s Lasagna', 'Favorite Childhood Song: \
      You\'re a Jerk', 'Something I Love to Watch But Cannot Do Myslef: \
      Spoken Word Poetry', 'Favortie Snack: Jamaican Beef Patty'];
      
  // Pick a random greeting.
  const quote = quotes[Math.floor(Math.random() * quotes.length)];

  // Add it to the page.
  const quoteContainer = document.getElementById('quote-container');
  quoteContainer.innerText = quote;
}

