import java.io.*;
import java.util.*;

public class Pager {
    
    public static void main(String args[]) throws IOException {
        
    int machineSize = Integer.parseInt(args[0]);
    int pageSize = Integer.parseInt(args[1]);
    int processSize = Integer.parseInt(args[2]);
    int jobMix = Integer.parseInt(args[3]);
    int references = Integer.parseInt(args[4]);
    String repAlgorithm = args[5];
    int outputLevel=Integer.parseInt(args[6]);
        
        /*int machineSize = 800;
        int pageSize = 40;
         int processSize =400;
         int jobMix = 4;
         int references = 5000;
         String repAlgorithm = "lru";
         int outputLevel=0;*/

         int quantum = 3;
         process processList[]=null; //array of all processes
         page[] pageList=null; // array of all pages for all processes. each page stores its process number
         Scanner random;
         pageList = new page[(machineSize / pageSize)];  // number of pages  //+1 ?? check 
         random = new Scanner(new File("random-numbers.txt"));
       
        // initialize all processes  // there is no process stored in 0th element of array //each process number corresponds to i  , no need to subtract add 1
        switch(jobMix) {
        case 1:{
            processList=new process[2];
            for(int i = 1; i <=1; i++) {
                processList[i]=new process(i, 1, 0, 0, references); // one process and  A=1 and B=C=0,
                }
            break;
            }
        
        case 2:{
            processList=new process[5];
            for(int i = 1; i <= 4; i++) {
            processList[i]= new process(i, 1, 0, 0, references);
            // 4 processList and A=1, B=C=0
            }
            break;
        }
        
        case 3:{
            processList=new process[5];
            for(int i = 1; i <=4; i++) {
                processList[i]= new process(i, 0, 0, 0, references);
                //Four processList, each with A=B=C=0 (fully random references).  
            }
        break; 
        }
      
      case 4:{
        processList=new process[5];
        processList[1]=new process(1, 0.75, 0.25, 0, references);
        processList[2]=new process(2, 0.75, 0, 0.25, references);
        processList[3]=new process(3, 0.75, 0.125, 0.125, references);
        processList[4]=new process(4, 0.5, 0.125, 0.125, references);
        break;
      }

        }//print all input
        print(machineSize, pageSize, processSize, jobMix, references, repAlgorithm);
        //simulate demand paging
        demandPager(processList, pageList, quantum, random, machineSize, pageSize, processSize, jobMix, references,repAlgorithm);
        
    }

    public static void demandPager(process[] processList, page[] pageList, int quantum, Scanner random, int machineSize, int pageSize, int processSize, int jobMix, int references, String replacementAlgorithm) {
        Queue<page> pageQueue = new LinkedList<page>(); //lru
        Stack<page> pageStack = new Stack<page>(); //lifo
        int time = 1;

        while(time <= ((processList.length-1) * references)) {
            for(int i=1;i<processList.length; i++ ){
                for(int j = 0; j < quantum && processList[i].numberOfReferences > 0; j++) {
                    int word = 0;
                    if(processList[i].isFirstReference) {
                        //word = (111 * processList[i].number) % processSize;
                        word = (111 * i) % processSize;
                        //System.out.println("word"+word); remove after debugging
                        processList[i].nextWord = word;
                        processList[i].isFirstReference = false; // no longer will be true for first reference / has been referenced once 
                    }
                    else {
                        word = (processList[i].nextWord + processSize) % processSize;
                    }
                    int pageNumber = word / pageSize;
                    page currentPage = new page(pageNumber, processList[i].number);
                    int frameNum = contains(processList , pageList, currentPage);

                    //if page exists
                    if(frameNum >= 0) {
                        if(replacementAlgorithm.equals("lru")) {
                            Iterator<page> iterator = pageQueue.iterator();
                            while (iterator.hasNext()) {
                                page pageOther = iterator.next();
                                if (currentPage.processNumber == pageOther.processNumber && currentPage.pageNumber == pageOther.pageNumber) {
                                    iterator.remove();
                                }
                            }
                            pageQueue.add(currentPage); //re add page so time is re stored
                        }
                    } 
                    //add page if didnt exist 
                    else {
                        int possiblyEvictedPage = addPage(processList, pageList, quantum, random , currentPage, pageQueue, pageStack, replacementAlgorithm, time);
                    }
                    //determine next word based on a, b, c
                    int nextRandom = random.nextInt();
                    //System.out.println("Hi "+ (processList[i].number+1)+" uses "+nextRandom); //remove after debugging
                    double y = nextRandom / (Integer.MAX_VALUE + 1d);

                    if(y < processList[i].A) {
                        processList[i].nextWord = (processList[i].nextWord + 1) % processSize;
                       // System.out.println("next word for "+processList[i].number + " is "+processList[i].nextWord); //remove after debugging
                    }
                    else if(y < processList[i].A + processList[i].B) {
                        processList[i].nextWord = (processList[i].nextWord - 5) % processSize;
                        //System.out.println("next word for "+processList[i].number + " is "+processList[i].nextWord); //remove after debugging
                    }
                    else if(y < processList[i].A + processList[i].B + processList[i].C) {
                        processList[i].nextWord = (processList[i].nextWord + 4) % processSize;
                    }
                        //System.out.println("next word for "+processList[i].number + " is "+processList[i].nextWord);                    }
                    /*else {
                        int RandomInt = (int)(Math.random() * ((processSize-1) + 1));
                        processList[i].nextWord = RandomInt % processSize;
                        //System.out.println("next word for "+processList[i].number + " is "+processList[i].nextWord);
                    }*/
                    else {
                        int nextRandomInt = random.nextInt();
                        processList[i].nextWord = nextRandomInt % processSize;
                    }

                    processList[i].numberOfReferences--;
                    time++;
                }
            }
        }
        
        int totalPageFaults = 0;
        float totalEvictions = 0;
        float averageResidencyTime = 0;


        //iterate through  processes and do final calculations
        for(int i=1;i<processList.length; i++ ){
            totalPageFaults +=  processList[i].numberOfPageFaults;
            totalEvictions +=  processList[i].numberOfEvictions;
            averageResidencyTime +=  processList[i].tResidencyTime;
            if(processList[i].numberOfEvictions == 0) 
                System.out.println("Process " + processList[i].number + " had " + processList[i].numberOfPageFaults +
                 " faults. With no evictions, the average residence is undefined.");
            
       
           else 
                System.out.println("Process " + processList[i].number + " had " + processList[i].numberOfPageFaults + " faults and " 
                + processList[i].tResidencyTime / processList[i].numberOfEvictions + " average residency ");
            }

        if(totalEvictions == 0) {
            System.out.printf("\nThe total number of faults is %d. With no evictions, the overage average residency is undefined\n", totalPageFaults);
        }

        else {
            averageResidencyTime = averageResidencyTime / totalEvictions;
            System.out.printf("\nThe total number of faults is %d and the overage average residency is %f\n \n", totalPageFaults, averageResidencyTime);
        }
           
        }

    public static int contains(process processList[],page[] pageList, page currentPage) {
        // search for current page
        for(int i = 0; i < pageList.length; i++) {
            if(pageList[i] == null) continue;
            //if page found
            if(pageList[i].processNumber == currentPage.processNumber && pageList[i].pageNumber == currentPage.pageNumber) {
                currentPage.loadTime = pageList[i].loadTime;
                return i;
            }
        }
        //System.out.print("out of bounds "+currentPage.processNumber); //debugging
        processList[currentPage.processNumber].numberOfPageFaults++;

        // not found
        return -1;

    }
    
    public static int addPage(process[] processList, page[] pageList, int quantum, Scanner random, page currentPage, Queue<page> pageQueue, Stack<page> pageStack, String repAlgorithm, int time) {
        //look for a free frame
        for(int i = pageList.length - 1; i >= 0; i--) {
            //if available
            if(pageList[i] == null) {
                pageList[i] = currentPage;
                if(repAlgorithm.equals("lru")) {
                    pageQueue.add(currentPage);
                }
                else if(repAlgorithm.equals("lifo")) {
                    pageStack.add(currentPage);
                }
                currentPage.loadTime = time;
                return i;
            }
        }
        //if no space evict based on replacement algorithm 
        return evict(processList, pageList, quantum, random, currentPage, pageQueue, pageStack, repAlgorithm, time);
    }
    
    public static int evict(process[] processList, page[] pageList, int quantum, Scanner random, page currentPage, Queue<page> pageQueue, Stack<page> pageStack, String repAlgorithm, int time) {
        switch(repAlgorithm){
        case "random":
        {
            int evict = random.nextInt() % pageList.length;
            page pageEvicted = pageList[evict];
            processList[pageEvicted.processNumber].numberOfEvictions++;
            processList[pageEvicted.processNumber].tResidencyTime += (time - pageEvicted.loadTime);
            pageList[evict] = currentPage;
            currentPage.loadTime = time;
            return evict;
        }
        /*{
            
            int r=random.nextInt();
            int evict=((r)% pageList.length);
            //System.out.println("while evicting uses random no"+r);
            //System.out.println("processSize  " + processSize);
            //int evict=((r-1)% pageList.length)+1; //pageList???;
            page evictPage=pageList[evict];     
            System.out.println("number"+ evict);
            processList[evictPage.processNumber].numberOfEvictions++;
            processList[evictPage.processNumber].tResidencyTime += (time - evictPage.loadTime);
            pageList[evict] = currentPage;
            currentPage.loadTime = time;
            return evict;
            
        }*/
        case "lifo":{
            page evictPage=pageStack.pop();
            for(int i = 0; i < pageList.length; i++) {
                if(pageList[i].processNumber == evictPage.processNumber && pageList[i].pageNumber == evictPage.pageNumber) {
                    processList[evictPage.processNumber].numberOfEvictions++;
                    pageList[i] = currentPage;
                    currentPage.loadTime = time;
                    processList[evictPage.processNumber].tResidencyTime += (time - evictPage.loadTime);
                    pageStack.add(currentPage);
                    return i;
                }
            }
            return -1;
        }
        
        case "lru":{
            page evictPage=pageQueue.poll();
            for(int i = 0; i < pageList.length; i++) {
                if(pageList[i].processNumber == evictPage.processNumber && pageList[i].pageNumber == evictPage.pageNumber) {
                    processList[evictPage.processNumber].numberOfEvictions++;
                    pageList[i] = currentPage;
                    currentPage.loadTime = time;
                    processList[evictPage.processNumber].tResidencyTime += (time - evictPage.loadTime);
                    pageQueue.add(currentPage);
                    return i;
                }   
            }
            return -1;
        }
        } //end of switch
        return -1; 
    }
    public static void print(int machineSize, int pageSize, int processSize, int jobMix, int references, String repAlgorithm) {
        System.out.println();
        System.out.println("The machine size is " + machineSize);
        System.out.println("The page size is " + pageSize);
        System.out.println("The process size is " + processSize);
        System.out.println("The job mix number is " + jobMix);
        System.out.println("The number of references per page is " + references);
        System.out.println("The replacement algorithm is " + repAlgorithm);
        System.out.println("The level of debugging output is 0");
        System.out.println();
    }
}
