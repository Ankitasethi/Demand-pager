public class process {

    //input data 
    public int number; // process number
    public int size; //size of process
    public int numberOfReferences;
    public String algorithm;
    public double A;
    public double B;
    public double C;
    
    //variables for values to be calculated and printed for each process
    public int numberOfPageFaults;
    public float numberOfEvictions;
    public float tResidencyTime; //total residency time for each proces
    public int nextWord;
   
    public boolean isFirstReference=true;
   
   // constructor w input parameters
    public process(int number, double a, double b, double c, int numberOfReferences){
        this.number = number;
        this.A = a;
        this.B = b;
        this.C = c;
        this.numberOfReferences = numberOfReferences;
        //this.isFirstReference=true;
    }
}
