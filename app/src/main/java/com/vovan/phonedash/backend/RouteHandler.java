package com.vovan.phonedash.backend;

import fi.iki.elonen.NanoHTTPD;

interface RouteHandler {
	NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session);
}
