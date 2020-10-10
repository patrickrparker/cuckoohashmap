   AUTHOR: Patrick Parker
   DATE: Nov 29, 2016
   GRADE NOW: Yes

1.  What test cases are needed, and in what order? 

    First tests are needed for the put and get which also tested getSize
    and isEmpty.  Next, I developed tests for containsKey and containsValue.
    After that, testing for clear and putAll together naturally followed.
    Next, I tested removing actual entries and entries not in the map.  
    
    Last, was all of the tests for the set views of the map.  The iterators
    had to be verified to traverse the entire set, throw the 
    NoSuchElementException if next is called after the last element is returned,
    throw the ConcurrentModificationException if the backing map is altered
    to have "fail-fast" behavior, and throw UnsupportedOperationException if
    remove is called.
    
    The sets were compared with an identical set using the Java HashMap 
    implementation to check contains, containsAll, size, and clear.  
    Last, all unsupported operations were confirmed to throw the 
    UnsupportedOperationException.
      
   
  

2.  What did you not anticipate (e.g. time allocation, test cases,
    algorithms, methods)?
    
    I did not anticipate testing what buckets the values should be in.  
    For testing remove, I had to add failed removes to get full coverage.  
    Additionally, I had to add a third map with identical keys but different 
    values, and a Collection of plain Strings to get full code coverage for 
    the containsAll/contains method in the EntrySet class.  
    My put method was not correct at first because I let it cycle based on the
    size and when the first entry to be placed was encountered again.

    
    
	
3.  What would you do differently next time?

	I would spend some more time on my test cases first.  I realize now that
	I could have used the getEntry method to test the position of the entries.

    
