class WebserviceController < ApplicationController

  def index 

  end

  def position
    @account = Account.where(:accountnr => params[:accountnr], :pass => params[:pass]).first
    @position = @account.positions.build
    @position.lat = params[:lat]
    @position.lng = params[:lng]
    @position.save
    render :json => @position
  end

  def positions
    @account = Account.where(:accountnr => params[:accountnr], :pass => params[:pass]).first
    @positions = @account.positions
    render :json => @positions
  end

end