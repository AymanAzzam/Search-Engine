package com.crawler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class IncrementFrequency extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static void main(String[] args) {

    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String url = request.getParameter("Query");
        String typeString = request.getParameter("Type");

        int type = typeString.equals("Image") ? 1 : 0;

        DBController dbController;
        int success = 0;
        try {
            dbController = new DBController();
            Connection conn = dbController.connect();


            if(type == 1) {
                success = dbController.incrementImageFrequency(conn, url);
            } else {
                success = dbController.incrementURLFrequency(conn, url);
            }
            
        } catch (SQLException | ClassNotFoundException e1) {
            // e1.printStackTrace();
        }

        String responseMsg = new String((success == 1)?"Successful!":"Failed!");

        System.out.println("Frequency:\t" + typeString + "\t" + url + "\t" + responseMsg);

        response.getWriter().print(responseMsg);
        response.getWriter().flush();
    }
    
}