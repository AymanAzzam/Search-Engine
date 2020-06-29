package com.crawler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;

public class Trends extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static void main(String[] args) {
       
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String country = request.getParameter("Country");

        DBController dbController;
        try {
            dbController = new DBController();
            Connection conn = dbController.connect();

            ArrayList<String> topTrend = dbController.getTopTrend(conn, country);

            conn.close();

            JSONArray jsonArray = new JSONArray(topTrend);

            response.getWriter().print(jsonArray);

        } catch (ClassNotFoundException | SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace(response.getWriter());
        }
        
        response.getWriter().flush();
    }
}