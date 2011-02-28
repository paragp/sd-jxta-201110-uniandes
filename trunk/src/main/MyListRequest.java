package main;

import main.Main.SearchWindow;
import net.jxta.peergroup.PeerGroup;
import net.jxta.share.client.CachedListContentRequest;
import net.jxta.share.client.ListContentRequest;

/** 
 * An implementation of ListContentRequest that will automatically update 
 * a SearchWindow as ContentAdvertisements are returned. 
 *  
 * @see ListContentRequest 
 * @see CachedListContentRequest 
 */  
class MyListRequest extends ListContentRequest {  
SearchWindow searchWindow = null;  
  
/** 
 * Initialize a list request that will be propagated throughout a given 
 * peer group.  Any ContentAdvertisement for which the string returned 
 * by getName() or getDescription() contains inSubStr 
 *  (case insensitive) is sent back in a list response. However, the 
 * list request isn't sent until activateRequest() is called. 
 *  
 * @see net.jxta.share.client.ListContentRequest 
 * @see net.jxta.share.client.ListContentRequest#ListContentRequest(net.jxta.peergroup.PeerGroup, java.lang.String) 
 */  
public MyListRequest(PeerGroup group, String inSubStr  
             ,SearchWindow searchWindow) {  
    super(group, inSubStr);  
    this.searchWindow = searchWindow;  
}  

/** 
 * This function is called each time more results are received. 
 */  
public void notifyMoreResults() {  
    if (searchWindow != null) {  
    //note: getResults() returns all of the ContentAdvertisements  
    //received so far, not just the ones that were in the last list  
    //response.  
    searchWindow.updateResults(getResults());  
    }  
}  
}
