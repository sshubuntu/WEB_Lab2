package sshubuntu.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "AreaCheckServlet", urlPatterns = {"/area-check"})
public class AreaCheckServlet extends HttpServlet {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {


        double x = Double.parseDouble(request.getParameter("x"));
        double y = Double.parseDouble(request.getParameter("y"));
        double r = Double.parseDouble(request.getParameter("r"));

        Coordinates point = new Coordinates(x, y, r);

        if (!point.isValidCoordinates()) {
            if (acceptsJson(request)) {
                writeJsonError(response);
            } else {
                request.getSession().setAttribute("error", "Invalid coordinates or radius");
                response.sendRedirect("/result.jsp");
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
                response.sendRedirect("/result.jsp");
            }
        }
    }

    private boolean acceptsJson(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("application/json");
    }

    private void writeJsonOk(HttpServletResponse response, Coordinates point) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> jsonResponse = new HashMap<>();
        jsonResponse.put("x", point.getX());
        jsonResponse.put("y", point.getY());
        jsonResponse.put("r", point.getR());
        jsonResponse.put("hit", point.isHit());
        jsonResponse.put("creationTime", point.getTime().format(DateTimeFormatter.ISO_LOCAL_DATE));

        objectMapper.writeValue(response.getWriter(), jsonResponse);
    }

    private void writeJsonError(HttpServletResponse response) throws IOException {
        response.setStatus(400);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, String> jsonResponse = new HashMap<>();
        jsonResponse.put("error", "Invalid coordinates or radius");

        objectMapper.writeValue(response.getWriter(), jsonResponse);
    }
}