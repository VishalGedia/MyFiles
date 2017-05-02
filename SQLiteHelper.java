package com.teezom.sqlHelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.teezom.global.AppConstant;
import com.teezom.models.AmountModel;
import com.teezom.models.MainGetLibraryModel;
import com.teezom.models.MainTransactionModel;
import com.teezom.models.MonthSelectModel;
import com.teezom.models.PBRecords;
import com.teezom.models.RecordsModel;
import com.teezom.models.ResultGetLibraryModel;
import com.teezom.models.TransactionModel;
import com.teezom.wsModels.MainLibraryModel;
import com.teezom.wsModels.MainLibraryModelWS;
import com.teezom.wsModels.MainTransactionModelWS;
import com.teezom.wsModels.MonthWiseRecordsModel;
import com.teezom.wsModels.NewTransactionModel;
import com.teezom.wsModels.WSLibraryModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

public class SQLiteHelper {

    SQLiteDatabase database;
    SQLiteHandler dbHandler;

    public SQLiteHelper(Context context) {
        dbHandler = new SQLiteHandler(context);
    }

    public void open() {
        database = dbHandler.getWritableDatabase();
    }

    public void close() {
        dbHandler.close();
    }

    public int insertData(String PBName, int progress, String month, int month_no, int year) {

        if (null != database) {
            ContentValues cv = new ContentValues();
            cv.put("progress_name", PBName);
            cv.put("progress_value", progress);
            cv.put("month", month);
            cv.put("month_no", month_no);
            cv.put("year", year);
            if (database.insert("records", null, cv) > 0) {
                return 1;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public ArrayList<MonthWiseRecordsModel> getAllRecordsWS() {

        ArrayList<MonthWiseRecordsModel> monthWiseRecordsModels = new ArrayList<>();

        Cursor c = database.rawQuery(
                "select SUM(Amount) as TotalAmount," +
                        "strftime('%d', tDate) as Daypart," +
                        "strftime('%m', tDate) as Monthpart," +
                        "strftime('%Y',tDate) as Yearpart," +
                        "count(*) as TotalCount," +
                        "type from library GROUP by tDate, type", null);

        while (c.moveToNext()) {

            MonthWiseRecordsModel recordsModel = new MonthWiseRecordsModel();
            recordsModel.setTotalAmount(c.getInt(0));
            recordsModel.setDayPart(c.getInt(1));
            recordsModel.setMonthPart(c.getInt(2));
            recordsModel.setYearPart(c.getInt(3));
            recordsModel.setTotalCount(c.getInt(4));
            recordsModel.setType(c.getInt(5));
            monthWiseRecordsModels.add(recordsModel);
        }

        return monthWiseRecordsModels;
    }

    public ArrayList<TransactionModel> getAllTransaction() {
        ArrayList<TransactionModel> transactionModels = new ArrayList<>();
        Cursor c = database.rawQuery("SELECT * FROM transactions", null);
        while (c.moveToNext()) {
            TransactionModel transactionModel = new TransactionModel();
            transactionModel.setId(c.getInt(0));
            transactionModel.setType(c.getString(1));
            transactionModel.setDate(c.getInt(2));
            transactionModel.setMonth(c.getInt(3));
            transactionModel.setYear(c.getInt(4));
            transactionModel.setAmount(c.getInt(5));
            transactionModel.setClient_vendor(c.getString(6));
            transactionModel.setDoc(c.getString(7));
        }
        return transactionModels;
    }

    public int getUserLibrary(
            int tId, int UserId, String FileUrl, int Type, String tDate, double Amount,
            String VendorName, String cDate, String ExpenseTypeName, int Hours, double HourlyRate) {

        if (null != database) {
            ContentValues cv = new ContentValues();
            cv.put("tId", tId);
            cv.put("UserId", UserId);
            cv.put("FileUrl", FileUrl);
            cv.put("type", Type);
            cv.put("tDate", tDate);
            cv.put("Amount", Amount);
            cv.put("VendorName", VendorName);
            cv.put("ExpenseTypeName", ExpenseTypeName);
            cv.put("cDate", cDate);
            cv.put("Hours", Hours);
            cv.put("HourlyRate", HourlyRate);
            if (database.insert("library", null, cv) > 0) {
                return 1;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    // Add amount in the available old amount
    public void updateRecords(int month, int year, int amount, String type) {

        String query = "update records set progress_value = progress_value + " + amount + " WHERE month_no = " + month + " AND year = " + year + " AND progress_name = '" + type + "'";
        database.execSQL(query);
    }

    public int updateData(int id, int progress) {

        ContentValues cv = new ContentValues();
        cv.put("progress_value", progress);

        int query = database.update("records", cv, "id = ?", new String[]{String.valueOf(id)});
        if (query > 0) {
            return 1;
        } else {
            return 0;
        }
    }

    public int insertTransaction(String type, int date, int month, int year, int amount, String client_vendor, String doc) {

        if (null != database) {
            ContentValues cv = new ContentValues();
            cv.put("type", type);
            cv.put("date", date);
            cv.put("month", month);
            cv.put("year", year);
            cv.put("amount", amount);
            cv.put("client_vendor", client_vendor);
            cv.put("doc", doc);
            if (database.insert("transactions", null, cv) > 0) {
                return 1;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public ArrayList<String> vendorsOrClients() {

        ArrayList<String> vendors = new ArrayList<>();
        Cursor c = database.rawQuery("SELECT distinct(VendorName) FROM library", null);
        while (c.moveToNext()) {
            vendors.add(c.getString(0));
        }
        return vendors;
    }

    public int insertUsers(String username, String email, String password) {

        if (null != database) {
            ContentValues cv = new ContentValues();
            cv.put("username", username);
            cv.put("email", email);
            cv.put("password", password);
            if (database.insert("appusers", null, cv) > 0) {
                return 1;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public String getUserPassword(String userName) {
        Cursor cursor = database.query("appusers", null, " username=?", new String[]{userName}, null, null, null);
        if (cursor.getCount() < 1) { // UserName Not Exist
            cursor.close();
            return "NOT EXIST";
        }
        cursor.moveToFirst();
        String password = cursor.getString(cursor.getColumnIndex("password"));
        cursor.close();
        return password;
    }

    public boolean checkUsernameExists(String username) {
        String query = "Select * from appusers where username like '" + username + "'";
        Cursor cursor = database.rawQuery(query, null);
        if (cursor.getCount() > 0) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    public boolean checkEmailExists(String email) {
        String query = "Select * from appusers where email like '" + email + "'";
        Cursor cursor = database.rawQuery(query, null);
        if (cursor.getCount() > 0) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    public ArrayList<PBRecords> getMonthWiseRecordsIncome(String name) {

        ArrayList<PBRecords> transactionModels = new ArrayList<>();

        Cursor c = database.rawQuery("SELECT * FROM records WHERE progress_name = '" + name + "' ORDER BY id DESC", null);

        if (c.getCount() > 0) {
            while (c.moveToNext()) {

                PBRecords pbRecords = new PBRecords();
                pbRecords.setId(c.getInt(0));
                pbRecords.setProgressName(c.getString(1));
                pbRecords.setProgress(c.getInt(2));
                pbRecords.setMonth(c.getString(3));
                pbRecords.setYear(c.getInt(5));

                transactionModels.add(pbRecords);
            }
        }

        return transactionModels;

    }

    public MainTransactionModel getAllTransactions(int month, int year) {

        MainTransactionModel mainTransactionModel = new MainTransactionModel();

        ArrayList<TransactionModel> incomeTransactionModels = new ArrayList<>();
        ArrayList<TransactionModel> expenseTransactionModels = new ArrayList<>();

        Cursor c = database.rawQuery("SELECT * FROM transactions WHERE month = '" + month + "' AND year = '" + year + "'", null);

        if (c.getCount() > 0) {
            while (c.moveToNext()) {

                if (c.getString(1).equals(AppConstant.INCOME)) {

                    TransactionModel transactionModel = new TransactionModel();
                    transactionModel.setId(c.getInt(0));
                    transactionModel.setType(c.getString(1));
                    transactionModel.setDate(c.getInt(2));
                    transactionModel.setMonth(c.getInt(3));
                    transactionModel.setYear(c.getInt(4));
                    transactionModel.setAmount(c.getInt(5));
                    transactionModel.setClient_vendor(c.getString(6));
                    transactionModel.setDoc(c.getString(7));

                    incomeTransactionModels.add(transactionModel);

                } else if (c.getString(1).equals(AppConstant.EXPENSE)) {

                    TransactionModel transactionModel = new TransactionModel();
                    transactionModel.setId(c.getInt(0));
                    transactionModel.setType(c.getString(1));
                    transactionModel.setDate(c.getInt(2));
                    transactionModel.setMonth(c.getInt(3));
                    transactionModel.setYear(c.getInt(4));
                    transactionModel.setAmount(c.getInt(5));
                    transactionModel.setClient_vendor(c.getString(6));
                    transactionModel.setDoc(c.getString(7));

                    expenseTransactionModels.add(transactionModel);

                }
            }
        }

        mainTransactionModel.setIncomeRecords(incomeTransactionModels);
        mainTransactionModel.setExpenseRecords(expenseTransactionModels);

        return mainTransactionModel;
    }

    public MainTransactionModelWS getAllTransactionsWS(String month, String year, int type) {

        MainTransactionModelWS mainTransactionModel = new MainTransactionModelWS();

        ArrayList<NewTransactionModel> transactionModels = new ArrayList<>();

        Cursor c = database.rawQuery("select FileUrl, Type, tDate, Amount, VendorName, strftime('%m', tDate) as MonthPart, strftime('%Y', tDate) as YearPart FROM library " +
                "WHERE MonthPart = '" + month + "' AND YearPart = '" + year + "' ", null);

        if (c.getCount() > 0) {
            while (c.moveToNext()) {

                if (c.getInt(1) == 1 && type == 0) {

                    NewTransactionModel transactionModel = new NewTransactionModel();
                    transactionModel.setFileUrl(c.getString(0));
                    transactionModel.setType(c.getInt(1));
                    transactionModel.setDate(c.getString(2));
                    transactionModel.setAmount(c.getInt(3));
                    transactionModel.setVendorName(c.getString(4));

                    transactionModels.add(transactionModel);

                } else if (c.getInt(1) == 2 && type == 1) {

                    NewTransactionModel transactionModel = new NewTransactionModel();
                    transactionModel.setFileUrl(c.getString(0));
                    transactionModel.setType(c.getInt(1));
                    transactionModel.setDate(c.getString(2));
                    transactionModel.setAmount(c.getInt(3));
                    transactionModel.setVendorName(c.getString(4));

                    transactionModels.add(transactionModel);
                }
            }
        }

        mainTransactionModel.setTransactionModels(transactionModels);

        return mainTransactionModel;
    }

    public MainGetLibraryModel getAllLibrary() {

        MainGetLibraryModel recordsModel = new MainGetLibraryModel();

        ArrayList<ResultGetLibraryModel> incomeRecords = new ArrayList<>();

        Cursor c = database.rawQuery("SELECT * FROM library", null);

        while (c.moveToNext()) {

            ResultGetLibraryModel pbRecords = new ResultGetLibraryModel();
            pbRecords.setID(c.getInt(1));
            pbRecords.setUserID(c.getInt(2));
            pbRecords.setFileurl(c.getString(3));
            pbRecords.setType(c.getInt(4));
            pbRecords.setTdate(c.getString(5));
            pbRecords.setAmount(c.getInt(6));
            pbRecords.setVendorName(c.getString(7));
            pbRecords.setCdate(c.getString(8));

            incomeRecords.add(pbRecords);
        }

        recordsModel.setResult(incomeRecords);

        return recordsModel;
    }

    public MainGetLibraryModel getAllLibraryMonthWise() {

        MainGetLibraryModel mainGetLibraryModel = new MainGetLibraryModel();

        ArrayList<ResultGetLibraryModel> resultGetLibraryModels = new ArrayList<>();

        Cursor c = database.rawQuery(
                "select sum(Amount) as TotalAmount, " +
                        "strftime('%d', tDate) as Daypart, " +
                        "strftime('%m', tDate) as Monthpart, " +
                        "strftime('%Y',tDate) as Yearpart,  " +
                        "count(*) as TotalCount from library GROUP by tDate",
                null
        );

        while (c.moveToNext()) {

            ResultGetLibraryModel pbRecords = new ResultGetLibraryModel();
            pbRecords.setID(c.getInt(1));
            pbRecords.setUserID(c.getInt(2));
            pbRecords.setFileurl(c.getString(3));
            pbRecords.setType(c.getInt(4));
            pbRecords.setTdate(c.getString(5));
            pbRecords.setAmount(c.getInt(6));
            pbRecords.setVendorName(c.getString(7));
            pbRecords.setCdate(c.getString(8));

            resultGetLibraryModels.add(pbRecords);
        }

        mainGetLibraryModel.setResult(resultGetLibraryModels);

        return mainGetLibraryModel;

    }

    public void deleteAll() {
        database.execSQL("delete from library");
    }

    public RecordsModel getAllRecords() {

        RecordsModel recordsModel = new RecordsModel();
        ArrayList<PBRecords> incomeRecords = new ArrayList<>();
        ArrayList<PBRecords> expenseRecords = new ArrayList<>();
        ArrayList<PBRecords> profitRecords = new ArrayList<>();

        Cursor c = database.rawQuery("SELECT * FROM records order by id desc", null);

        while (c.moveToNext()) {

            if (c.getString(1).equals(AppConstant.INCOME)) {

                PBRecords pbRecords = new PBRecords();
                pbRecords.setId(c.getInt(0));
                pbRecords.setProgressName(c.getString(1));
                pbRecords.setProgress(c.getInt(2));
                pbRecords.setMonth(c.getString(3));
                pbRecords.setYear(c.getInt(5));

                incomeRecords.add(pbRecords);

            } else if (c.getString(1).equals(AppConstant.EXPENSE)) {

                PBRecords pbRecords = new PBRecords();
                pbRecords.setId(c.getInt(0));
                pbRecords.setProgressName(c.getString(1));
                pbRecords.setProgress(c.getInt(2));
                pbRecords.setMonth(c.getString(3));
                pbRecords.setYear(c.getInt(5));

                expenseRecords.add(pbRecords);

            } else if (c.getString(1).equals(AppConstant.PROFIT)) {

                PBRecords pbRecords = new PBRecords();
                pbRecords.setId(c.getInt(0));
                pbRecords.setProgressName(c.getString(1));
                pbRecords.setProgress(c.getInt(2));
                pbRecords.setMonth(c.getString(3));
                pbRecords.setYear(c.getInt(5));

                profitRecords.add(pbRecords);

            }
        }

        recordsModel.setIncomeRecords(incomeRecords);
        recordsModel.setExpenseRecords(expenseRecords);
        recordsModel.setProfitRecords(profitRecords);

        return recordsModel;
    }

    public RecordsModel getAllLibraryHome() {

        RecordsModel recordsModel = new RecordsModel();
        ArrayList<PBRecords> incomeRecords = new ArrayList<>();
        ArrayList<PBRecords> expenseRecords = new ArrayList<>();
        ArrayList<PBRecords> profitRecords = new ArrayList<>();

        Cursor c = database.rawQuery("SELECT * FROM records order by id desc", null);

        while (c.moveToNext()) {

            if (c.getString(1).equals(AppConstant.INCOME)) {

                PBRecords pbRecords = new PBRecords();
                pbRecords.setId(c.getInt(0));
                pbRecords.setProgressName(c.getString(1));
                pbRecords.setProgress(c.getInt(2));
                pbRecords.setMonth(c.getString(3));
                pbRecords.setYear(c.getInt(5));

                incomeRecords.add(pbRecords);

            } else if (c.getString(1).equals(AppConstant.EXPENSE)) {

                PBRecords pbRecords = new PBRecords();
                pbRecords.setId(c.getInt(0));
                pbRecords.setProgressName(c.getString(1));
                pbRecords.setProgress(c.getInt(2));
                pbRecords.setMonth(c.getString(3));
                pbRecords.setYear(c.getInt(5));

                expenseRecords.add(pbRecords);

            } else if (c.getString(1).equals(AppConstant.PROFIT)) {

                PBRecords pbRecords = new PBRecords();
                pbRecords.setId(c.getInt(0));
                pbRecords.setProgressName(c.getString(1));
                pbRecords.setProgress(c.getInt(2));
                pbRecords.setMonth(c.getString(3));
                pbRecords.setYear(c.getInt(5));

                profitRecords.add(pbRecords);
            }
        }

        recordsModel.setIncomeRecords(incomeRecords);
        recordsModel.setExpenseRecords(expenseRecords);
        recordsModel.setProfitRecords(profitRecords);

        return recordsModel;

    }

    public MainLibraryModel getAllRecordsMonthWise() {

        MainLibraryModel recordsModel = new MainLibraryModel();

        ArrayList<MonthWiseRecordsModel> incomeRecords = new ArrayList<>();
        ArrayList<MonthWiseRecordsModel> expenseRecords = new ArrayList<>();

        Cursor c = database.rawQuery(
                "select sum(Amount) as TotalAmount, " +
                        "strftime('%d', tDate) as DayPart, " +
                        "strftime('%m', tDate) as MonthPart, " +
                        "strftime('%Y', tDate) as YearPart,  " +
                        "count(*) as TotalCount, " +
                        "type " +
                        "from library GROUP by tDate, type", null);

        while (c.moveToNext()) {

            if (c.getInt(5) == 1) {

                MonthWiseRecordsModel pbRecords = new MonthWiseRecordsModel();
                pbRecords.setTotalAmount(c.getInt(0));
                pbRecords.setDayPart(c.getInt(1));
                pbRecords.setMonthPart(c.getInt(2));
                pbRecords.setYearPart(c.getInt(3));
                pbRecords.setTotalCount(c.getInt(4));
                pbRecords.setType(c.getInt(5));

                incomeRecords.add(pbRecords);

            } else if (c.getInt(5) == 2) {

                MonthWiseRecordsModel pbRecords = new MonthWiseRecordsModel();
                pbRecords.setTotalAmount(c.getInt(0));
                pbRecords.setDayPart(c.getInt(1));
                pbRecords.setMonthPart(c.getInt(2));
                pbRecords.setYearPart(c.getInt(3));
                pbRecords.setTotalCount(c.getInt(4));
                pbRecords.setType(c.getInt(5));

                expenseRecords.add(pbRecords);

            }
        }

        recordsModel.setIncomeModel(incomeRecords);
        recordsModel.setExpenseModel(expenseRecords);

        return recordsModel;
    }

    public MainLibraryModelWS getAllRecordsMonthWise2(int pos) {

        MainLibraryModelWS mainLibraryModel = new MainLibraryModelWS();

        ArrayList<WSLibraryModel> mainLibraryModels = new ArrayList<>();

        Cursor c = database.rawQuery(
                "select count(*) as TotalCount, tDate, " +
                        "sum(case when type = '1' then Amount else 0 end) \"Income\"," +
                        "sum(case when type = '2' then Amount else 0 end) \"Expenses\", " +
                        "(sum(case when type = '1' then Amount else 0 end) - sum(case when type = '2' then Amount else 0 end)) \"Profit\", " +
                        "strftime('%d', tDate) as DayPart, " +
                        "strftime('%m', tDate) as MonthPart, " +
                        "strftime('%Y', tDate) as YearPart " +
                        "from library " +
                        "group by MonthPart, YearPart Order by date(tDate) DESC", null);

        while (c.moveToNext()) {

            if (pos == 0) {

                Cursor c2 = database.rawQuery(
                        "select count(*) as TotalIncomeCount from library " +
                                "where type = '1' AND " +
                                "strftime('%Y', tDate) = '" + c.getInt(7) + "' AND " +
                                "strftime('%m', tDate) = '" + c.getString(6) + "'", null);

                WSLibraryModel recordsModel = new WSLibraryModel();

                while (c2.moveToNext()) {
                    recordsModel.setTotalCountIncome(c2.getString(0));
                }
                recordsModel.settDate(c.getString(1));
                recordsModel.setAmount(c.getInt(2));
                recordsModel.setDayPart(c.getInt(5));
                recordsModel.setMonthPart(c.getString(6));
                recordsModel.setYearPart(c.getInt(7));

                mainLibraryModels.add(recordsModel);

            } else if (pos == 1) {

                Cursor c2 = database.rawQuery(
                        "select count(*) as TotalExpenseCount from library " +
                                "where type = '2' AND " +
                                "strftime('%Y', tDate) = '" + c.getInt(7) + "' AND " +
                                "strftime('%m', tDate) = '" + c.getString(6) + "'", null);

                WSLibraryModel recordsModel = new WSLibraryModel();

                while (c2.moveToNext()) {
                    recordsModel.setTotalCountExpense(c2.getString(0));
                }
                recordsModel.settDate(c.getString(1));
                recordsModel.setAmount(c.getInt(3));
                recordsModel.setDayPart(c.getInt(5));
                recordsModel.setMonthPart(c.getString(6));
                recordsModel.setYearPart(c.getInt(7));

                mainLibraryModels.add(recordsModel);

            } else if (pos == 2) {

                WSLibraryModel recordsModel = new WSLibraryModel();

                recordsModel.setTotalCountProfit(" - ");
                recordsModel.settDate(c.getString(1));
                recordsModel.setAmount(c.getInt(4));
                recordsModel.setDayPart(c.getInt(5));
                recordsModel.setMonthPart(c.getString(6));
                recordsModel.setYearPart(c.getInt(7));

                mainLibraryModels.add(recordsModel);
            }

        }
        mainLibraryModel.setRecords(mainLibraryModels);

        return mainLibraryModel;
    }

    public ArrayList<String> monthAndYear() {

        ArrayList<String> monthSelectModels = new ArrayList<>();

        Cursor c = database.rawQuery(
                "select strftime('%d', tDate) as DayPart, " +
                        "strftime('%m', tDate) as MonthPart, " +
                        "strftime('%Y', tDate) as YearPart, " +
                        "count(*) as TotalCount " +
                        "from library " +
                        "group by YearPart, MonthPart Order by date(tDate) DESC", null);

        while (c.moveToNext()) {
            String data = c.getString(1) + ", " + c.getString(2);
            monthSelectModels.add(data);
        }

        return monthSelectModels;
    }

    public AmountModel getAmount(String month, int year, int pos) {

        AmountModel amountModel = new AmountModel();

        Cursor c = database.rawQuery(
                "select count(*) as TotalCount, tDate," +
                        "SUM(case when type = '1' then Amount else 0 end) as Income, " +
                        "SUM(case when type = '2' then Amount else 0 end) as Expenses , " +
                        "(SUM(case when type = '1' then Amount else 0 end) - SUM(case when type = '2' then Amount else 0 end)) as Profit, " +
                        "strftime('%d', tDate) as DayPart, " +
                        "strftime('%m', tDate) as MonthPart, " +
                        "strftime('%Y', tDate) as YearPart, " +
                        "ExpenseTypeName, " +
                        "Hours, " +
                        "HourlyRate " +
                        "from library " +
                        "where MonthPart='" + month + "' AND YearPart='" + year + "' " +
                        "group by MonthPart, YearPart", null);

        while (c.moveToNext()) {

            if (pos == 0) {
                Cursor c2 = database.rawQuery(
                        "select count(*) as TotalIncomeCount from library " +
                                "where type = '1' AND " +
                                "strftime('%Y', tDate) = '" + year + "' AND " +
                                "strftime('%m', tDate) = '" + month + "'", null);
                amountModel.setAmount(c.getInt(2));
                amountModel.setHours(c.getInt(9));
                amountModel.setHourlyRate(c.getDouble(10));

                while (c2.moveToNext()) {
                    amountModel.setCount(c2.getString(0));
                }

            } else if (pos == 1) {
                Cursor c2 = database.rawQuery(
                        "select count(*) as TotalExpenseCount, " +
                                "SUM(case when ExpenseTypeName = 'Core ' then Amount else 0 end) as CoreExpense , " +
                                "SUM(case when ExpenseTypeName = 'Marketing' then Amount else 0 end) as MarketingExpense , " +
                                "SUM(case when ExpenseTypeName = 'Professional Services' then Amount else 0 end) as ProfessionalExpense " +
                                "from library " +
                                "where type = '2' AND " +
                                "strftime('%Y', tDate) = '" + year + "' AND " +
                                "strftime('%m', tDate) = '" + month + "'", null);

                // Aug  Marketing       8000
                // Jul  Marketing       600
                // Jul  Professional    200

                amountModel.setAmount(c.getInt(3));
                while (c2.moveToNext()) {
                    amountModel.setCount(c2.getString(0));
                    amountModel.setCoreExpense(c2.getInt(1));
                    amountModel.setMarketingExpense(c2.getInt(2));
                    amountModel.setProfessionalExpense(c2.getInt(3));
                }

            } else if (pos == 2) {
                amountModel.setAmount(c.getInt(4));
                amountModel.setCount(" - ");
            }
        }

        return amountModel;
    }

    public ArrayList<String> months() {

        ArrayList<String> vendors = new ArrayList<>();
        Cursor c = database.rawQuery(
                "select count(*) as TotalCount, tDate, " +
                        "sum(case when type = '1' then Amount else 0 end) \"Income\", " +
                        "sum(case when type = '2' then Amount else 0 end) \"Expenses\", " +
                        "(sum(case when type = '1' then Amount else 0 end) - sum(case when type = '2' then Amount else 0 end)) \"Profit\", " +
                        "strftime('%d', tDate) as DayPart, " +
                        "strftime('%m', tDate) as MonthPart, " +
                        "strftime('%Y', tDate) as YearPart " +
                        "from library " +
                        "group by MonthPart, YearPart Order by date(tDate) DESC", null);
        while (c.moveToNext()) {
            vendors.add(String.valueOf(c.getInt(6)));
        }
        return vendors;
    }

    public String currentMonthIncomeTransaction(String currentYear, String currentMonth) {

        String total = null;

        Cursor c2 = database.rawQuery(
                "select count(*) as TotalExpenseCount from library " +
                        "where type = '1' AND " +
                        "strftime('%Y', tDate) = '" + currentYear + "' AND " +
                        "strftime('%m', tDate) = '" + currentMonth + "'", null);

        while (c2.moveToNext()) {
            total = c2.getString(0);
        }

        return total;
    }

    public MainLibraryModelWS getAllRecordsMonthWise3(int pos, String monthString, String limit) {

        MainLibraryModelWS mainLibraryModel = new MainLibraryModelWS();

        ArrayList<WSLibraryModel> mainLibraryModels = new ArrayList<>();

        Cursor c = database.rawQuery(
                "select count(*) as TotalCount, tDate, " +
                        "sum(case when type = '1' then Amount else 0 end) \"Income\", " +
                        "sum(case when type = '2' then Amount else 0 end) \"Expenses\", " +
                        "(sum(case when type = '1' then Amount else 0 end) - sum(case when type = '2' then Amount else 0 end)) \"Profit\", " +
                        "strftime('%d', tDate) as DayPart, " +
                        "strftime('%m', tDate) as MonthPart, " +
                        "strftime('%Y', tDate) as YearPart " +
                        "from library " +
                        "group by MonthPart, YearPart Order by date(tDate) DESC", null);

        while (c.moveToNext()) {

            int month = c.getInt(6);
            String modifiedMonth = (month < 10 ? "0" : "") + month;

            if (pos == 0) {

                Cursor c2 = database.rawQuery(
                        "select count(*) as TotalIncomeCount from library " +
                                "where type = '1' AND " +
                                "strftime('%Y', tDate) = '" + c.getInt(7) + "' AND " +
                                "strftime('%m', tDate) = '" + modifiedMonth + "'", null);

                WSLibraryModel recordsModel = new WSLibraryModel();

                while (c2.moveToNext()) {
                    recordsModel.setTotalCountIncome(c2.getString(0));
                }
                recordsModel.settDate(c.getString(1));
                recordsModel.setAmount(c.getInt(2));
                recordsModel.setDayPart(c.getInt(5));
                recordsModel.setMonthPart(c.getString(6));
                recordsModel.setYearPart(c.getInt(7));

                mainLibraryModels.add(recordsModel);

            } else if (pos == 1) {

                Cursor c2 = database.rawQuery(
                        "select count(*) as TotalExpenseCount from library " +
                                "where type = '2' AND " +
                                "strftime('%Y', tDate) = '" + c.getInt(7) + "' AND " +
                                "strftime('%m', tDate) = '" + modifiedMonth + "'", null);

                WSLibraryModel recordsModel = new WSLibraryModel();

                while (c2.moveToNext()) {
                    recordsModel.setTotalCountExpense(c2.getString(0));
                }
                recordsModel.settDate(c.getString(1));
                recordsModel.setAmount(c.getInt(3));
                recordsModel.setDayPart(c.getInt(5));
                recordsModel.setMonthPart(c.getString(6));
                recordsModel.setYearPart(c.getInt(7));

                mainLibraryModels.add(recordsModel);

            } else if (pos == 2) {

                WSLibraryModel recordsModel = new WSLibraryModel();

                recordsModel.setTotalCountProfit(" - ");
                recordsModel.settDate(c.getString(1));
                recordsModel.setAmount(c.getInt(4));
                recordsModel.setDayPart(c.getInt(5));
                recordsModel.setMonthPart(c.getString(6));
                recordsModel.setYearPart(c.getInt(7));

                mainLibraryModels.add(recordsModel);

            }

        }
        mainLibraryModel.setRecords(mainLibraryModels);
        return mainLibraryModel;
    }

    public MainTransactionModel getCurrentMonthTransactions(int month, int year) {

        MainTransactionModel mainTransactionModel = new MainTransactionModel();

        ArrayList<TransactionModel> incomeTransactionModels = new ArrayList<>();
        ArrayList<TransactionModel> expenseTransactionModels = new ArrayList<>();

        Cursor c = database.rawQuery("SELECT SUM(amount) as TotalAmount, type from transactions WHERE month=" + month + " and year = " + year + " group by type", null);

        if (c.getCount() > 0) {

            while (c.moveToNext()) {

                if (c.getString(1).equals(AppConstant.INCOME)) {

                    TransactionModel transactionModel = new TransactionModel();
                    transactionModel.setAmount(c.getInt(0));

                    incomeTransactionModels.add(transactionModel);

                } else if (c.getString(1).equals(AppConstant.EXPENSE)) {

                    TransactionModel transactionModel = new TransactionModel();
                    transactionModel.setAmount(c.getInt(0));

                    expenseTransactionModels.add(transactionModel);
                }
            }
        }

        mainTransactionModel.setIncomeRecords(incomeTransactionModels);
        mainTransactionModel.setExpenseRecords(expenseTransactionModels);

        return mainTransactionModel;
    }

    public ArrayList<Integer> getRecordsCount(int no) {

        ArrayList<Integer> count = new ArrayList<>();

        Cursor c = database.rawQuery(
                "select count(*) as TotalCount, tDate, " +
                        "strftime('%d', tDate) as DayPart, " +
                        "strftime('%m', tDate) as MonthPart, " +
                        "strftime('%Y', tDate) as YearPart " +
                        "from library " +
                        "where type = '" + no + "' " +
                        "group by MonthPart, YearPart", null);

        while (c.moveToNext()) {
            count.add(c.getInt(0));
        }

        return count;
    }

    public int getCurrentMonthTransactionsWS(int pos, String month, int year) {

        int count = 0;

        Cursor c = database.rawQuery(
                "select count(*) as TotalCount, tDate, " +
                        "SUM(case when type = '1' then Amount else 0 end) as Income, " +
                        "SUM(case when type = '2' then Amount else 0 end) as Expenses, " +
                        "(SUM(case when type = '1' then Amount else 0 end) - SUM(case when type = '2' then Amount else 0 end)) as Profit, " +
                        "strftime('%d', tDate) as DayPart, " +
                        "strftime('%m', tDate) as MonthPart, " +
                        "strftime('%Y', tDate) as YearPart " +
                        "from library " +
                        "where MonthPart='" + month + "' AND YearPart='" + year + "' " +
                        "group by MonthPart, YearPart", null);

        if (c.getCount() > 0) {

            while (c.moveToNext()) {

                if (pos == 0) {
                    count = c.getInt(2);
                } else if (pos == 1) {
                    count = c.getInt(3);
                } else if (pos == 2) {
                    count = c.getInt(4);
                }
            }
        }
        return count;
    }

    public String getOldestDate() {

        String oldestDate = null;

        Cursor c = database.rawQuery("SELECT Min(tDate) from Library", null);

        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                oldestDate = c.getString(0);
            }
        }

        return oldestDate;
    }

    /*public MainLibraryModelWS getAllRecordsMonthWise2(int pos, int no) {

        MainLibraryModelWS mainLibraryModel = new MainLibraryModelWS();

        ArrayList<WSLibraryModel> mainLibraryModels = new ArrayList<>();

        Cursor c = database.rawQuery(
                "select *, count(*) as TotalCount, " +
                        "sum(case when type = '1' then Amount else 0 end) \"Income\", " +
                        "sum(case when type = '2' then Amount else 0 end) \"Expenses\", " +
                        "(sum(case when type = '1' then Amount else 0 end) - sum(case when type = '2' then Amount else 0 end)) \"Profit\", " +
                        "strftime('%d', tDate) as DayPart, " +
                        "strftime('%m', tDate) as MonthPart, " +
                        "strftime('%Y', tDate) as YearPart " +
                        "from library " +
                        "group by MonthPart, YearPart", null);

        while (c.moveToNext()) {

            if (pos == 0 && c.getInt(4) == 1) {

                WSLibraryModel recordsModel = new WSLibraryModel();

                recordsModel.setTotalCount(c.getString(9));
                recordsModel.settDate(c.getString(5));
                recordsModel.setAmount(c.getInt(10));
                recordsModel.setDayPart(c.getInt(13));
                recordsModel.setMonthPart(c.getInt(14));
                recordsModel.setYearPart(c.getInt(15));

                mainLibraryModels.add(recordsModel);

            } else if (pos == 1) {

                if(c.getInt(4) == 2){
                    WSLibraryModel recordsModel = new WSLibraryModel();

                    recordsModel.setTotalCount(c.getString(9));
                    recordsModel.settDate(c.getString(5));
                    recordsModel.setAmount(c.getInt(11));
                    recordsModel.setDayPart(c.getInt(13));
                    recordsModel.setMonthPart(c.getInt(14));
                    recordsModel.setYearPart(c.getInt(15));

                    mainLibraryModels.add(recordsModel);
                }

            } else if (pos == 2) {

                WSLibraryModel recordsModel = new WSLibraryModel();

                recordsModel.setTotalCount(c.getString(9));
                recordsModel.settDate(c.getString(5));
                recordsModel.setAmount(c.getInt(12));
                recordsModel.setDayPart(c.getInt(13));
                recordsModel.setMonthPart(c.getInt(14));
                recordsModel.setYearPart(c.getInt(15));

                mainLibraryModels.add(recordsModel);
            }
        }
        mainLibraryModel.setRecords(mainLibraryModels);
        return mainLibraryModel;
    }*/

    public MainLibraryModelWS getAllRecordsMonthWiseProfit() {

        MainLibraryModelWS mainLibraryModel = new MainLibraryModelWS();

        ArrayList<WSLibraryModel> mainLibraryModels = new ArrayList<>();

        Cursor c = database.rawQuery(
                "select count(*) as TotalCount, tDate, type, " +
                        "sum(case when type = '1' then Amount else 0 end) \"Income\", " +
                        "sum(case when type = '2' then Amount else 0 end) \"Expenses\", " +
                        "(sum(case when type = '1' then Amount else 0 end) - sum(case when type = '2' then Amount else 0 end)) \"Profit\", " +
                        "strftime('%d', tDate) as DayPart, " +
                        "strftime('%m', tDate) as MonthPart, " +
                        "strftime('%Y', tDate) as YearPart " +
                        "from library " +
                        "group by MonthPart", null);

        while (c.moveToNext()) {
            WSLibraryModel recordsModel = new WSLibraryModel();

            recordsModel.setTotalCountProfit(c.getString(0));
            recordsModel.settDate(c.getString(1));
            recordsModel.setAmount(c.getInt(5));
            recordsModel.setDayPart(c.getInt(6));
            recordsModel.setMonthPart(c.getString(7));
            recordsModel.setYearPart(c.getInt(8));

            mainLibraryModels.add(recordsModel);
        }
        mainLibraryModel.setRecords(mainLibraryModels);
        return mainLibraryModel;
    }

    public RecordsModel getAllRecordsAndTransactions() {

        RecordsModel recordsModel = new RecordsModel();
        ArrayList<PBRecords> incomeRecords = new ArrayList<>();
        ArrayList<PBRecords> expenseRecords = new ArrayList<>();
        ArrayList<PBRecords> profitRecords = new ArrayList<>();

        Cursor c = database.rawQuery("SELECT *, (SELECT COUNT(*) FROM transactions WHERE month = month_no AND year = year AND type = progress_name) as NoRecords FROM records order by id desc", null);

        while (c.moveToNext()) {

            if (c.getString(1).equals(AppConstant.INCOME)) {

                PBRecords pbRecords = new PBRecords();
                pbRecords.setId(c.getInt(0));
                pbRecords.setProgressName(c.getString(1));
                pbRecords.setProgress(c.getInt(2));
                pbRecords.setMonth(c.getString(3));
                pbRecords.setYear(c.getInt(5));
                pbRecords.setTransactionCount(c.getInt(6));

                incomeRecords.add(pbRecords);

            } else if (c.getString(1).equals(AppConstant.EXPENSE)) {

                PBRecords pbRecords = new PBRecords();
                pbRecords.setId(c.getInt(0));
                pbRecords.setProgressName(c.getString(1));
                pbRecords.setProgress(c.getInt(2));
                pbRecords.setMonth(c.getString(3));
                pbRecords.setYear(c.getInt(5));
                pbRecords.setTransactionCount(c.getInt(6));

                expenseRecords.add(pbRecords);

            } else if (c.getString(1).equals(AppConstant.PROFIT)) {

                PBRecords pbRecords = new PBRecords();
                pbRecords.setId(c.getInt(0));
                pbRecords.setProgressName(c.getString(1));
                pbRecords.setProgress(c.getInt(2));
                pbRecords.setMonth(c.getString(3));
                pbRecords.setYear(c.getInt(5));
                pbRecords.setTransactionCount(c.getInt(6));

                profitRecords.add(pbRecords);

            }
        }

        recordsModel.setIncomeRecords(incomeRecords);
        recordsModel.setExpenseRecords(expenseRecords);
        recordsModel.setProfitRecords(profitRecords);

        return recordsModel;
    }

    public String getAllDataAndGenerateJSON() throws JSONException, FileNotFoundException {

        String query = "SELECT * FROM transactions";
        Cursor c = database.rawQuery(query, null);
        c.moveToFirst();
        JSONObject rootObject = new JSONObject();
        JSONArray ContactArray = new JSONArray();
        File f = new File(Environment.getExternalStorageDirectory() + "/ContactDetail.txt");
        FileOutputStream fos = new FileOutputStream(f, true);
        PrintStream ps = new PrintStream(fos);

        int i = 0;
        while (!c.isAfterLast()) {

            JSONObject contact = new JSONObject();
            try {
                contact.put("id", c.getString(c.getColumnIndex("id")));
                contact.put("type", c.getString(c.getColumnIndex("type")));
                contact.put("date", c.getString(c.getColumnIndex("date")));
                contact.put("month", c.getString(c.getColumnIndex("month")));
                contact.put("year", c.getString(c.getColumnIndex("year")));
                contact.put("amount", c.getString(c.getColumnIndex("amount")));
                contact.put("client_vendor", c.getString(c.getColumnIndex("client_vendor")));
                contact.put("doc", c.getString(c.getColumnIndex("doc")));

                c.moveToNext();

                ContactArray.put(i, contact);
                i++;

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        rootObject.put("transactions", ContactArray);
        ps.append(rootObject.toString());
        return rootObject.toString();
    }

}
