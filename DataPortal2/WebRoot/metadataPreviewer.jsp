<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>

<!DOCTYPE html>

<html lang="en">

<head>
<title>LTER :: Network Data Portal</title>

<meta charset="UTF-8" />
<meta content="width=device-width, initial-scale=1, maximum-scale=1" name="viewport">

<link rel="shortcut icon" href="./images/favicon.ico" type="image/x-icon" />

<!-- Google Fonts CSS -->
<link href="https://fonts.googleapis.com/css?family=Open+Sans:400,300,600,300italic" rel="stylesheet" type="text/css">

<!-- Page Layout CSS MUST LOAD BEFORE bootstap.css -->
<link href="css/style_slate.css" media="all" rel="stylesheet" type="text/css">

<!-- JS -->
<script src="js/jqueryba3a.js?ver=1.7.2" type="text/javascript"></script>
<script src="bootstrap/js/bootstrap68b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/jquery.easing.1.368b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/jquery.flexslider-min68b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/themeple68b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/jquery.pixel68b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/jquery.mobilemenu68b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/isotope68b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/mediaelement-and-player.min68b368b3.js?ver=1" type="text/javascript"></script>

<!-- Mobile Device CSS -->
<link href="bootstrap/css/bootstrap.css" media="screen" rel="stylesheet" type="text/css">
<link href="bootstrap/css/bootstrap-responsive.css" media="screen" rel="stylesheet" type="text/css">

<!-- Metadata Viewer -->
<script src="js/toggle.js" type="text/javascript"></script>
<!-- /Metadata Viewer -->

</head>

<body>

<jsp:include page="header.jsp" />

<div class="row-fluid ">
		<div class="container">
			<div class="row-fluid distance_1">
				<div class="box_shadow box_layout">
					<div class="row-fluid">
						<div class="row-fluid">
							<div class="span12">
							
		<div class="content">

			<h2 align="center">Metadata Previewer</h2>

      <fieldset>
        <legend>Preview Metadata Rendering</legend>

        <p>Preview a rendered version of EML in HTML format by uploading your
           EML file</p>

        <div class="section">
          <form id="metadataPreviewer" name="metadataPreviewer" method="post"
            action="./metadataPreviewer" enctype="multipart/form-data"
            target="_blank">
            <table align="left" cellpadding="4em">
              <tbody>
                <tr>
                  <td align="left" width="130px"><label for="packageid">File:</label>
                  </td>
                  <td align="left" width="200px"><input type="file"
                    name="emlfile" accept="application/xml" size="60" 
                    required="required" /></td>
                  <td align="center" width="70px">
                    <input type="submit" name="upload" value="Upload" />
                  </td>
                  <td align="center" width="40px">
                    <input type="reset" name="reset" value="Clear" />
                  </td>
                </tr>
              </tbody>
            </table>
          </form>
        </div>
      </fieldset>


		</div>
		<!-- end of content -->
							  
						  </div>
					</div>
				</div>
			</div>
		</div>
	</div>

		<jsp:include page="footer.jsp" />
		
</div>

        <script type="text/javascript">
        jQuery(document).ready(function() {
          jQuery(".toggleButton").click(function() {
            jQuery(this).next(".collapsible").slideToggle("fast");
          });
          jQuery(".collapsible").hide();
          jQuery("#toggleSummary").next(".collapsible").show();
        });    
        jQuery("#showAll").click(function() {
          jQuery(".collapsible").show();
        });
        jQuery("#hideAll").click(function() {
          jQuery(".collapsible").hide();
        });
        </script>  

</body>

</html>

