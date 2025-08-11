import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

public class CustomerBillData {

    public static void main(String[] args)
    {

   String inputData = "{\r\n" +
"    \"name\": \"Sabareesan\",\r\n" +
"    \"dob\": \"1980-12-21\",\r\n" +
"    \"bills\": [\r\n" +
"        {\r\n" +
"            \"date\": \"2022-12-03T05:34:57.660Z\",\r\n" +
"            \"products\": [\r\n" +
"                {\r\n" +
"                    \"skuCode\": \"a\",\r\n" +
"                    \"price\": 100,\r\n" +
"                    \"quantity\": 2,\r\n" +
"                    \"taxAmt\": 20\r\n" +
"                },\r\n" +
"                {\r\n" +
"                    \"skuCode\": \"b\",\r\n" +
"                    \"price\": 200,\r\n" +
"                    \"quantity\": 3,\r\n" +
"                    \"taxAmt\": 0\r\n" +
"                }\r\n" +
"            ],\r\n" +
"            \"discount\": \"10%\"\r\n" +
"        },\r\n" +
"        {\r\n" +
"            \"date\": \"2023-08-03T05:34:57.660Z\",\r\n" +
"            \"products\": [\r\n" +
"                {\r\n" +
"                    \"skuCode\": \"c\",\r\n" +
"                    \"price\": 80,\r\n" +    
"                    \"quantity\": 1,\r\n" +
"                    \"taxAmt\": 20\r\n" +
"                }\r\n" +
"            ],\r\n" +
"            \"discount\": \"0%\"\r\n" +
"        }\r\n" +
"    ]\r\n" +
"}";

         JSONObject obj=new JSONObject(inputData);
         int age=calculateAge(obj.getString("dob"));
         obj.put("age",age);
         String lastBillDate=getLastBillDate(obj.getJSONArray("bills"));
         System.out.println(lastBillDate);
         calculateGrossAmount(obj.getJSONArray("bills"));
         calculatePaidAmount(obj.getJSONArray("bills"));
         payableAmount(obj.getJSONArray("bills"));
         int lifeTimeValue=calculateLifeTimeValue(obj.getJSONArray("bills"));
         obj.put("ltv",lifeTimeValue);
         System.out.println(lifeTimeValue);
         
       isBoughtForBirthDay(obj.getJSONArray("bills"),obj.getString("dob"));
   
         System.out.println(obj);

}

public static int calculateAge(String Dob)
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
                 netAmount=netAmount+(productObj.getInt("price")*productObj.getInt("quantity"))+productObj.getInt("taxAmt");
            }
            billObj.put("grossTotal",netAmount);
        }

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
                 int netAmount=(productObj.getInt("price")*productObj.getInt("quantity"))+productObj.getInt("taxAmt");
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