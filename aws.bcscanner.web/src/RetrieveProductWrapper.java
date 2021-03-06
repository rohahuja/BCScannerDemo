import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import aws.bcscanner.lambda.product.*;

/**
 * Servlet implementation class RetrieveProductServlet
 */
@WebServlet("/RetrieveProductWrapper")
public class RetrieveProductWrapper extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	/**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	response.setContentType("text/html");
        response.setHeader("Cache-control", "no-cache, no-store");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "-1");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setHeader("Access-Control-Max-Age", "86400");
        
        String barcode = request.getParameter("json");
        
        // TODO HACK - to deal with Json parsing issues. Possibly use another Json
  		// library (org.json)
        barcode = barcode.replace("\"",  "");
        
        try (PrintWriter out = response.getWriter()) {
  			RetrieveProduct lambdaFunction = new RetrieveProduct();
            String lambdaResponse = lambdaFunction.handleRequest(barcode,  null);
            out.println(lambdaResponse);
        }
    }
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RetrieveProductWrapper() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		processRequest(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		processRequest(request, response);
	}

}
