import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class CustomerBillData {

    public static void main(String[] args)
    {

        String JSONFilePath="bill.json";
        String inputData="";
        try{
            inputData=new String(Files.readAllBytes(Paths.get(JSONFilePath)));
        }catch(IOException e)
        {
            return ;
        }

         JSONObject obj=new JSONObject(inputData);
         JSONArray bills=obj.getJSONArray("bills");

         int age=calculateAge(obj.getString("dob"),bills);
         obj.put("age",age);

         String lastBillDate=getLastBillDate(obj.getJSONArray("bills"));
         obj.put("lastBillDate",lastBillDate); 

         calculateGrossAmount(bills);
         calculatePaidAmount(bills);
         payableAmount(bills);

         int lifeTimeValue=calculateLifeTimeValue(bills);
         obj.put("ltv",lifeTimeValue);
        
         isBoughtForBirthDay(obj.getJSONArray("bills"),obj.getString("dob"));

         String outputFilePath = "output.json";
        try{
            Files.write(Paths.get(outputFilePath),obj.toString(2).getBytes());
        }catch(IOException e){
            System.err.println("Failed to write output file: " + e.getMessage());
        }
        

}

public static int calculateAge(String Dob,JSONArray bills)
    {
        String[] parsedDob=Dob.split("-");
        int birthYear=Integer.parseInt(parsedDob[0]);
        int birthMonth=Integer.parseInt(parsedDob[1]);
        int birthDate=Integer.parseInt(parsedDob[2]);

        LocalDate current=LocalDate.now();
        String[] parsedCurrentDate=(current.toString()).split("-");
        int currentYear=Integer.parseInt(parsedCurrentDate[0]);
        int currentMonth=Integer.parseInt(parsedCurrentDate[1]);
        int currentDate=Integer.parseInt(parsedCurrentDate[2]);

        int age=currentYear-birthYear;
        if(currentMonth<birthMonth ||((currentMonth==birthMonth)&& (currentDate<birthDate))){
            age--;
        }
        return age;

    }


    public static String getLastBillDate(JSONArray bills){
       
        String latestBillDate ="";
        for(int i=0;i<bills.length();i++)
        {
            String billDate=bills.getJSONObject(i).getString("date");
            if(billDate.compareTo(latestBillDate)>0)
            {
                    latestBillDate  =billDate;
            }
            
        }
        return latestBillDate;


    }


    public static void  calculateGrossAmount(JSONArray bills){
        for(int i=0;i<bills.length();i++)
        {
            int netAmount=0;
            JSONObject billObj=bills.getJSONObject(i);
            JSONArray productArray=billObj.getJSONArray("products");
            for(int j=0;j<productArray.length();j++)
            {
                JSONObject productObj=productArray.getJSONObject(j);
                 netAmount=netAmount+calculateNetAmount(productObj.getInt("price"),productObj.getInt("quantity"),productObj.getInt("taxAmt"));
            }
            billObj.put("grossTotal",netAmount);
        }

    }
    public static int calculateNetAmount(int price,int quantity,int taxAmt)
    {
        return (price*quantity)+taxAmt;
    }

    public static void calculatePaidAmount(JSONArray bills)
    {
         for(int i=0;i<bills.length();i++)
        {
            JSONObject billObj=bills.getJSONObject(i);
            JSONArray productArray=billObj.getJSONArray("products");
            for(int j=0;j<productArray.length();j++)
            {
                JSONObject productObj=productArray.getJSONObject(j);
                 int netAmount=calculateNetAmount(productObj.getInt("price"),productObj.getInt("quantity"),productObj.getInt("taxAmt"));
                 int discount=Integer.parseInt(billObj.getString("discount").replaceAll("%", ""));
                 int paidAmount=netAmount-(discount*(productObj.getInt("price")*productObj.getInt("quantity"))/100);
                 productObj.put("paidAmount",paidAmount);

            }
           
        }

    }


    public static void payableAmount(JSONArray bills)
    {
        for(int i=0;i<bills.length();i++)
        {
            JSONObject billObj=bills.getJSONObject(i);
            int netAmount=0;
            JSONArray productArray=billObj.getJSONArray("products");
            for(int j=0;j<productArray.length();j++)
            {
                JSONObject productObj=productArray.getJSONObject(j);
                netAmount=netAmount+productObj.getInt("paidAmount");
            }
            billObj.put("payableAmt",netAmount);
        }
    }




    public static int calculateLifeTimeValue(JSONArray bills)
    {

        int ltv=0;
        for(int i=0;i<bills.length();i++)
        {
            JSONObject billObj=bills.getJSONObject(i);
             ltv=ltv+billObj.getInt("payableAmt");
        }
        return ltv;

    }

  public static void isBoughtForBirthDay(JSONArray bills, String DOB) {
    LocalDate dobDate=LocalDate.parse(DOB);
    int dobMonth=dobDate.getMonthValue();
    int dobDay=dobDate.getDayOfMonth();

    
    LocalDate dobFixed=LocalDate.of(2000, dobMonth, dobDay);

    for (int i=0; i<bills.length();i++) {
        JSONObject billObj=bills.getJSONObject(i);
        String extractbill=billObj.getString("date");
        LocalDate billDate=LocalDate.parse(extractbill.substring(0, 10));
        int billMonth=billDate.getMonthValue();
        int billDay=billDate.getDayOfMonth();
        LocalDate billFixed=LocalDate.of(2000, billMonth, billDay);

        long diffDays=Math.abs(ChronoUnit.DAYS.between(dobFixed, billFixed));

      
        if (diffDays>180) { 
            diffDays=365-diffDays; 
        }

        if (diffDays<=30) {
            billObj.put("boughtForBirthDay", true);
        } else {
            billObj.put("boughtForBirthDay", false);
        }
    }
}
}
