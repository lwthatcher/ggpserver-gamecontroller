<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:import href="../generic/template.xsl"/>
	<xsl:import href="../generic/state.xsl"/>

	<xsl:param name="width" select="500"/>
	<xsl:param name="height" select="500"/>
	<xsl:param name="xcells" select="6"/>
	<xsl:param name="ycells" select="6"/>

	<xsl:template name="print_state">
		<xsl:call-template name="print_pilgrimage_board"/>

		<xsl:call-template name="state">
			<xsl:with-param name="excludeFluent" select="'BUILDER'"/>
			<xsl:with-param name="excludeFluent2" select="'PILGRIM'"/>
			<xsl:with-param name="excludeFluent3" select="'CONTROL'"/>
			<xsl:with-param name="excludeFluent4" select="'CELL'"/>
			<xsl:with-param name="excludeFluent5" select="'PHASE'"/>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="print_pilgrimage_board">
		<xsl:variable name="cellwidth" select="$width div $xcells - 4"/>
		<xsl:variable name="cellheight" select="$height div $ycells - 4"/>
		<div> <!-- Set Style -->
			<style type="text/css" media="all">
				div.at {
					float: left;
					width: <xsl:value-of select="$cellwidth"/>px;
					height: <xsl:value-of select="$cellheight"/>px;
					border: 2px solid #000;
					background-color: #004400;
				}
				div.board {
					background-color: #004400;
					width: <xsl:value-of select="$width"/>px;
					height: <xsl:value-of select="$height"/>px;
				}
				img.piece {
					width: <xsl:value-of select="$cellwidth * 0.8"/>px;
					height: <xsl:value-of select="$cellheight * 0.8"/>px;
					padding-left: <xsl:value-of select="$cellwidth * 0.1"/>px;
					padding-right: <xsl:value-of select="$cellwidth * 0.1"/>px;
					padding-top: <xsl:value-of select="$cellheight * 0.1"/>px;
					padding-bottom: <xsl:value-of select="$cellheight * 0.1"/>px;
				}
			</style>

			<!-- Draw Board -->
			<xsl:call-template name="pilgrimage_board">
				<xsl:with-param name="cols" select="$xcells"/>
				<xsl:with-param name="rows" select="$ycells"/>
			</xsl:call-template>
		</div>
	</xsl:template>

	<xsl:template name="at">
		<xsl:param name="row" select="1"/>
		<xsl:param name="col" select="1"/>

		<div class="at">
		<xsl:attribute name="id">
			<xsl:value-of select="'at_'"/>
			<xsl:value-of select="$row"/>
			<xsl:value-of select="$col"/>
		</xsl:attribute>

		<xsl:choose>
			<xsl:when test="fact[prop-f='CELL' and arg[1] = $row and arg[2] = $col and arg[3]=1]">
			<xsl:attribute name="style">background-color: #226622</xsl:attribute>
			</xsl:when>
			<xsl:when test="fact[prop-f='CELL' and arg[1] = $row and arg[2] = $col and arg[3]=2]">
			<xsl:attribute name="style">background-color: #669922</xsl:attribute>
			</xsl:when>
			<xsl:when test="fact[prop-f='CELL' and arg[1] = $row and arg[2] = $col and arg[3]=3]">
			<xsl:attribute name="style">background-color: #99BB22</xsl:attribute>
			</xsl:when>
			<xsl:when test="fact[prop-f='CELL' and arg[1] = $row and arg[2] = $col and arg[3]=4]">
			<xsl:attribute name="style">background-color: #BBDD22</xsl:attribute>
			</xsl:when>
			<xsl:when test="fact[prop-f='CELL' and arg[1] = $row and arg[2] = $col and arg[3]=5]">
			<xsl:attribute name="style">background-color: #FFFF22</xsl:attribute>
			</xsl:when>
		</xsl:choose>

		<center>
		<xsl:choose>
			<xsl:when test="fact[prop-f='BUILDER' and arg[2]=$row and arg[3]=$col and arg[1]='RED']">
			<img class="piece">
				<xsl:attribute name="src"><xsl:value-of select="$stylesheetURL"/>pilgrimage/red.png</xsl:attribute>
			</img>
			</xsl:when>
			<xsl:when test="fact[prop-f='BUILDER' and arg[2]=$row and arg[3]=$col and arg[1]='BLUE']">
			<img class="piece">
				<xsl:attribute name="src"><xsl:value-of select="$stylesheetURL"/>pilgrimage/blue.png</xsl:attribute>
			</img>
			</xsl:when>
			<xsl:when test="fact[prop-f='PILGRIM' and arg[2]=$row and arg[3]=$col and arg[1]='RED']">
			<img class="piece">
				<xsl:attribute name="src"><xsl:value-of select="$stylesheetURL"/>pilgrimage/Red_Bishop.png</xsl:attribute>
			</img>
			</xsl:when>
			<xsl:when test="fact[prop-f='PILGRIM' and arg[2]=$row and arg[3]=$col and arg[1]='BLUE']">
			<img class="piece">
				<xsl:attribute name="src"><xsl:value-of select="$stylesheetURL"/>pilgrimage/Blue_Bishop.png</xsl:attribute>
			</img>
			</xsl:when>
		</xsl:choose>
		</center>
		</div>
	</xsl:template>

	<xsl:template name="board_row">
		<xsl:param name="cols" select="1"/>
		<xsl:param name="rows" select="1"/>
		<xsl:param name="row" select="1"/>
		<xsl:param name="col" select="1"/>

		<xsl:call-template name="at">
			<xsl:with-param name="row" select="$row"/>
			<xsl:with-param name="col" select="$col"/>
		</xsl:call-template>
		<xsl:if test="$col &lt; $cols">
			<xsl:call-template name="board_row">
			<xsl:with-param name="cols" select="$cols"/>
			<xsl:with-param name="rows" select="$rows"/>
			<xsl:with-param name="row" select="$row"/>
			<xsl:with-param name="col" select="$col + 1"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>

	<xsl:template name="board_rows">
		<xsl:param name="cols" select="1"/>
		<xsl:param name="rows" select="1"/>
		<xsl:param name="row" select="1"/>
			<xsl:call-template name="board_row">
				<xsl:with-param name="cols" select="$cols"/>
				<xsl:with-param name="rows" select="$rows"/>
				<xsl:with-param name="row" select="$row"/>
			</xsl:call-template>
		<xsl:if test="$row &lt; $rows">
			<xsl:call-template name="board_rows">
				<xsl:with-param name="cols" select="$cols"/>
				<xsl:with-param name="rows" select="$rows"/>
				<xsl:with-param name="row" select="$row + 1"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>

	<xsl:template name="pilgrimage_board">
		<xsl:param name="cols" select="1"/>
		<xsl:param name="rows" select="1"/>
		<div class="board">
		<xsl:call-template name="board_rows">
			<xsl:with-param name="cols" select="$cols"/>
			<xsl:with-param name="rows" select="$rows"/>
		</xsl:call-template>
		</div>
	</xsl:template>

</xsl:stylesheet>









