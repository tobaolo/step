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

package com.google.sps;

import java.util.Array;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // List of all attendees requested
    Set<String> attendeesList = new HashSet<>(request.getAttendees());

    // Creates a map wth the key as the perosn's name and an empty list of their events.
    HashMap<String, ArrayList<TimeRange>> scheduleMap = 
        new HashMap<String, ArrayList<TimeRange>>();

    // Add a key (name of atendee) and empty list of attendee's events.
    for (String name : attendeesList) {
      scheduleMap.put(name, new ArrayList<TimeRange>());
    }
    
    // Iterate through all events, check if one of the requested attendees is in the listed event,
    // and add to each person's individual schedule.
    for (Event event : events) {
      for (String name : event.getAttendees()) {
        if (attendeesList.contains(name)) {
          scheduleMap.get(name).add(event);
        }
      }
    }

    // Iterate through each person's schedule and create TimeRanges for their free times whose
    // duration is greater than or equal to duration of the request.
    HashMap<String, ArrayList<TimeRange>> availabilityMap = 
        new HashMap<String, ArrayList<TimeRange>>();
    for (Map.Entry<String, ArrayList<TimeRange>> personSchedule : availabilityMap) {
      String name = personSchedule.getKey();
      ArrayList<TimeRange> scheduleList = personSchedule.getValue();
      ArrayList<TimeRange> freeTimeList = new ArrayList<TimeRange>();

      // Iterate through each event in the schedule and calculate free time between.
      // Start index 0 at beginning of day.
      int scheduleSize = scheduleList.size();
      for (int i = 0; i <= scheduleSize; i++) {
        TimeRange previousEvent;
        TimeRange nextEvent;
        int startFreeTime;
        int endFreeTime;
        int durationFreeTime = startFreeTime - endFreeTime;

        // From the start of the day to the beginning of the first event.
        if (i.equals(0)) {
          startFreeTime = TimeRange.START_OF_DAY;
          nextEvent = scheduleList[i];
          endFreeTime = nextEvent.start();

        // From the end of the last event to the end of the day.       
        } else if (i.equals(scheduleSize)) {
          previousEvent = scheduleList[i-1];
          startFreeTime = previousEvent.start() + previousEvent.duration();
          endFreeTime = TimeRange.END_OF_DAY;

        // In between events during the day.
        } else {
          previousEvent = scheduleList[i-1];
          startFreeTime = previousEvent.start() + previousEvent.duration();
          nextEvent = scheduleList[i];
          endFreeTime = nextEvent.start();
        }

        // Validate if duration of free time is gretaer than or equal to duration of request.
        if (durationFreeTime >= request.getDuration()) {
          TimeRange freeTime = new TimeRange(startFreeTime, durationFreeTime);
          freeTimeList.add(freeTime);
        }
      }

      availabilityMap.put(name, freeTimeList);
    }

    System.out.print(availabilityMap);
  }
}


