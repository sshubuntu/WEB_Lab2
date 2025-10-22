package sshubuntu.server;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "AreaCheckServlet", urlPatterns = {"/area-check"})
public class AreaCheckServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        processRequest(request, response);
    }
    
    private void processRequest(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        try {
            double x = Double.parseDouble(request.getParameter("x"));
            double y = Double.parseDouble(request.getParameter("y"));
            double r = Double.parseDouble(request.getParameter("r"));
            
            Coordinates point = new Coordinates(x, y, r);
            
            if (!point.isValidCoordinates()) {
                if (acceptsJson(request)) {
                    writeJsonError(response, 400, "Некорректные координаты или радиус");
                } else {
                    request.getSession().setAttribute("error", "Некорректные координаты или радиус");
                    response.sendRedirect(request.getContextPath() + "/result.jsp");
                }
                return;
            }

            synchronized (this) {
                @SuppressWarnings("unchecked")
                List<Coordinates> results = (List<Coordinates>) getServletContext().getAttribute("results");
                if (results == null) {
                    results = new ArrayList<>();
                }

                results.add(point);
                getServletContext().setAttribute("results", results);

                if (acceptsJson(request)) {
                    writeJsonOk(response, point);
                } else {
                    response.sendRedirect(request.getContextPath() + "/result.jsp");
                }
            }
        } catch (NumberFormatException e) {
            if (acceptsJson(request)) {
                writeJsonError(response, 400, "Некорректный формат чисел");
            } else {
                request.getSession().setAttribute("error", "Некорректный формат чисел");
                response.sendRedirect(request.getContextPath() + "/result.jsp");
            }
        } catch (Exception e) {
            if (acceptsJson(request)) {
                writeJsonError(response, 500, "Ошибка сервера: " + e.getMessage());
            } else {
                request.getSession().setAttribute("error", "Ошибка сервера: " + e.getMessage());
                response.sendRedirect(request.getContextPath() + "/result.jsp");
            }
        }
    }

    private boolean acceptsJson(HttpServletRequest request){
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("application/json");
    }

    private void writeJsonOk(HttpServletResponse response, Coordinates point) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String time = point.getTime().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String json = "{"+
                "\"x\":" + point.getX() + ","+
                "\"y\":" + point.getY() + ","+
                "\"r\":" + point.getR() + ","+
                "\"hit\":" + point.isHit() + ","+
                "\"creationTime\":\"" + time + "\""+
                "}";
        out.write(json);
        out.flush();
    }

    private void writeJsonError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String json = "{"+
                "\"error\":\"" + escapeJson(message) + "\""+
                "}";

        out.write(json);
        out.flush();
    }

    private String escapeJson(String s){
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

