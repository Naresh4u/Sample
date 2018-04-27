package com.example.camel;


public class SuccessResponse {
	private RespnseStatus status = null;

	public RespnseStatus getStatus() {
		return status;
	}

	public void setStatus(RespnseStatus status) {
		this.status = status;
	}
	
	@Override
	public String toString() {
		return "[Status=" + status +"]";
	}
}
