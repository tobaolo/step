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

    // Checks if duration of request is longer than duration of day.
    if (request.getDuration() > (TimeRange.END_OF_DAY - TimeRange.START_OF_DAY + 1)) {
      Collection<TimeRange> empty = new ArrayList<TimeRange>();
      return empty;
    }

    // Creates a TimeRange for all day.
    TimeRange allDay = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TimeRange.END_OF_DAY, true);

    // List of all attendees requested
    Set<String> attendeesList = new HashSet<>(request.getAttendees());

    // Returns the whole day if there are no attendees.
    if (attendeesList.size() == 0){
      Collection<TimeRange> allDayList = new ArrayList<TimeRange>();
      allDayList.add(allDay);
      return allDayList;
    }

    // Creates a map wth the key as the perosn's name and an empty list of their events.
    HashMap<String, ArrayList<Event>> scheduleMap = 
        new HashMap<String, ArrayList<Event>>();

    // Add a key (name of atendee) and empty list of attendee's events.
    for (String name : attendeesList) {
      scheduleMap.put(name, new ArrayList<Event>());
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
    for (Map.Entry<String, ArrayList<Event>> personSchedule : scheduleMap.entrySet()) {
      String name = personSchedule.getKey();

      // List of one person's schedule.
      ArrayList<Event> scheduleList = personSchedule.getValue();

      // List of free times for one person.
      ArrayList<TimeRange> freeTimeList = new ArrayList<TimeRange>();
      int scheduleSize = scheduleList.size();

      // Checks if there are no events in the schedule and therefore free all day.
      if (scheduleList.isEmpty()) {
        freeTimeList.add(allDay);
      } else {
        // Iterate through each event in the schedule and calculate free time between.
        // Start index 0 at beginning of day.
        for (int i = 0; i <= scheduleSize; i++) {
          Integer refInt = new Integer(i);
          TimeRange previousEventTimeRange;
          TimeRange nextEventTimeRange;
          int startFreeTime;
          int endFreeTime;

          // From the start of the day to the beginning of the first event.
          if (refInt.equals(0)) {
            startFreeTime = TimeRange.START_OF_DAY;
            nextEventTimeRange = scheduleList.get(i).getWhen();
            endFreeTime = nextEventTimeRange.start();

          // From the end of the last event to the end of the day.
          } else if (refInt.equals(scheduleSize)) {
            previousEventTimeRange = scheduleList.get(i-1).getWhen();
            startFreeTime = previousEventTimeRange.end();
            endFreeTime = TimeRange.END_OF_DAY + 1;

          // In between events during the day.
          } else {
            previousEventTimeRange = scheduleList.get(i-1).getWhen();
            startFreeTime = previousEventTimeRange.end();
            nextEventTimeRange = scheduleList.get(i).getWhen();
            endFreeTime = nextEventTimeRange.start();
          }

          // Validate if duration of free time is gretaer than or equal to duration of request.
          int durationFreeTime = endFreeTime - startFreeTime;
          if (durationFreeTime >= request.getDuration()) {
            TimeRange freeTime = TimeRange.fromStartDuration(startFreeTime, durationFreeTime);
            freeTimeList.add(freeTime);
          }
        }
      }

      availabilityMap.put(name, freeTimeList);
    }
    System.out.println("===========================================================================");
    System.out.println(availabilityMap);

    // Chekcs if none of the attendees have any free time in their schedule.
    if (attendeesList.size() > 0 && availabilityMap.size() == 0) {
      return null;
    }

    ArrayList<ArrayList<TimeRange>> freeTimeList = new ArrayList<ArrayList<TimeRange>>
        (availabilityMap.values());
    System.out.println("tttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt");
    ArrayList<TimeRange> finalList = getAvailable(freeTimeList, request.getDuration());
    System.out.println(finalList);
    
    

    return finalList;
  }

  /**
   * Compares time ranges and returns a time range of the overlap. Boolean nextPerson checks 
   * whether there is another person to compare time ranges with.
   */
  private ArrayList<TimeRange> getAvailable(ArrayList<ArrayList<TimeRange>> allFreeTimeList, 
      long requestDuration) {
        ArrayList<TimeRange> availableOverlapList = new ArrayList<TimeRange>();
        Integer allFreeTimeListSize = new Integer(allFreeTimeList.size());
        
        // Validates the list is not empty.
        if (allFreeTimeListSize.equals(0)) {
          return null;
        }

        // If only one person is requestd, return their free time.
        if (allFreeTimeListSize.equals(1)) {
          return allFreeTimeList.get(0);
        }

        // Compares the first 2 TimeRange in freeTimeList and returns the overlap if only 2 
        // elements in freeTimeList or calls getAvailable recursively by replacing the first 2 
        // TimeRanges with their overlapped TimeRange
        ArrayList<TimeRange> freeTimeList1 = allFreeTimeList.get(0);
        ArrayList<TimeRange> freeTimeList2 = allFreeTimeList.get(1);

        // Compares each time range for the second person to the time ranges of the first person.
        for (TimeRange range1 : freeTimeList1) {
          for (TimeRange range2 : freeTimeList2) {

            // Checks if there is an overlap between the 2 ranges.
            if (range1.overlaps(range2)) {
              long overlapStart = Math.max(range1.start(), range2.start());
              long overlapEnd = Math.min(range1.end(), range2.end());
              long overlapDuration = overlapEnd - overlapStart;
            
              // Verifies that the overlap duration is greater than or equal to request duration.
              if (overlapDuration >= requestDuration) {
                TimeRange overlapTimeRange = TimeRange.fromStartDuration((int) overlapStart, (int) overlapDuration);

                // If there are more requested attendees in the allFreeTimeList, must compare their
                // free time with the overlapped free time of previous requested attendees.
                if (allFreeTimeListSize > 2) {
                  ArrayList<TimeRange> overlapArray = new ArrayList<TimeRange>();
                  overlapArray.add(overlapTimeRange);
                  ArrayList<ArrayList<TimeRange>> newAllFreeTimeList = new ArrayList<ArrayList<TimeRange>>(allFreeTimeList.subList(2, allFreeTimeListSize));
                  newAllFreeTimeList.add(0, overlapArray);
                  availableOverlapList.addAll(getAvailable(newAllFreeTimeList, requestDuration));
                }

                availableOverlapList.add(overlapTimeRange);
              }
            }
          }
        }
        
        return availableOverlapList;
  }
}


