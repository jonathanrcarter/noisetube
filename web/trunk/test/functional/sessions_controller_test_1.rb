require File.dirname(__FILE__) + '/../test_helper'

module SessionsTestHelpers
  
  def setup_shared
    #MessageTemplate.delete_all
    #UsersController.any_instance.stubs(:load_translations).returns(true)

    #User.delete_all
    #User.any_instance.stubs(:emailer_callbacks).returns(true)
    #@user = new_user(:password => 'passwd')
    @user=User.first
    #@invited_user = create_invited_user
  end
  
  def setup_forgot_password
    create_forgot_password_message_template        
  end
  
  def set_remember_me_for(user, expires = (Time.now + 365.days))
    set_auth_cookie(user, expires)
  end
  
  def set_auth_cookie(user, expires)
    login_as(user)
    @request.cookies["auth_token"] = CGI::Cookie.new('name' => 'auth_token', 'value' => "#{user.id};#{user.invitation_code}", 'expires' => expires)
    @request.cookies["L"] = CGI::Cookie.new('name' => 'L', 'value' => "#{user.id}", 'expires' => expires)    
  end
  
end




context "GET login screen", ActionController::TestCase do
  use_controller SessionsController  
  include SessionsTestHelpers
  
  setup do
    setup_shared
  end
  
  specify "should not redirect if already logged in" do
    login_as @user
    get :new
    status.should.be :success
    assert_logged_in(@user)
  end
  
  specify 'when not logged in, should show form, and forgot_password link' do
    get :new
    status.should.be :success
    assert_select "form" do
      assert_select "input#login"
      assert_select "input#password"
      assert_select "input[type=submit]"
    end
    assert_select "a[href=/forgot_password]"    
    assert_logged_out
  end  
  
  specify 'should redirect if already has auth cookie' do
    set_remember_me_for(@user)
    get :new
    assert_logged_in(@user)
    should.be.redirected_to home_path
  end
  
  specify 'should fail with an invalid cookie value' do
    @request.cookies["auth_token"] = CGI::Cookie.new('name' => 'auth_token', 'value' => "#{@user.id};wrong", 'expires' => Time.now+365.days)    
    get :new
    status.should.be :success
    assert_no_flash    
    assert_logged_out    
  end
  
  xspecify 'should fail with expired cookie' do
    set_remember_me_for(@user, 5.minutes.ago)    

    get :new
    status.should.be :success
    assert_no_flash    
    assert_logged_out
  end
end




context "Logging in", ActionController::TestCase do
  use_controller SessionsController  
  include SessionsTestHelpers
  
  setup do
    setup_shared
  end
  
  specify "should authenticate redirect and set cookie L=user_id if valid" do
    post :create, :login => @user.login, :password => @user.password
    should.be.redirected_to home_path
    assert_logged_in(@user)
    assert_equal @user.id.to_s, @response.cookies["L"]
  end

  specify "should fail if invalid" do
    post :create, :login => @user.login, :password => 'bad password'
    status.should.be :success
    assert_logged_out
    assert_flash(:error)
  end

  specify 'should set "Remember Me" cookie if checked in' do
    post :create, :login => @user.login, :password => @user.password, :remember_me => "on"
    assert_not_nil @response.cookies["auth_token"]
  end

  specify 'should NOT set "Remember Me" cookie if checked out' do
    post :create, :login => @user.login, :password => @user.password
    assert_nil @response.cookies["auth_token"]
  end
  
  specify 'should NOT work if user is only invited' do
    @invited_user.password = 'passwd'
    @invited_user.save!
    post :create, :login => @invited_user.email, :password => @invited_user.password
    assert_logged_out
    assert_flash(:error)
  end
  
  specify '[JSON] should return 200 when valid' do
    post :create, :login => @user.login, :password => @user.password, :format => 'json'
    assert_logged_in(@user)
    assert_response :success
    assert_json_response({ :status => '200 OK', :logged_in => true })
  end
  
  specify '[JSON] should return 422 when invalid' do
    post :create, :login => @user.login, :password => 'bad password', :format => 'json'
    assert_json_response({ :status => '422 Unprocessable Entity', :logged_in => false })    
  end
  
end



context "Logging Out", ActionController::TestCase do
  use_controller SessionsController  
  include SessionsTestHelpers
  
  setup do
    setup_shared
  end
  
  specify 'should work and remove token and cookie' do
    login_as @user
    get :destroy
    should.be.redirected_to home_path
    assert_logged_out
    assert_nil @response.cookies["auth_token"]
    assert_nil @response.cookies["L"]
  end
  
end



context "Forgot Password", ActionController::TestCase do
  use_controller SessionsController  
  include SessionsTestHelpers
  
  setup do
    setup_shared
    setup_forgot_password
  end
  
  specify 'GET should show form if logged out' do
    get :forgot_password
    status.should.be :success
    assert_select "form#forgot_password[method=post][action=/forgot_password]" do
      assert_select "input#email"
      assert_select "input[type=submit]"
    end
  end

  specify 'GET should redirect if already logged in' do
    login_as @user
    get :forgot_password
    should.be.redirected_to edit_user_path(@user)
  end  
  
  
  specify 'POST should send email if login valid and user active' do
    assert_email_sent do
      post :forgot_password, { :email => @user.login } 
      assert_response :redirect
      assert_flash(:notice)
    end
    
    # password should not change yet
    @user.crypted_password.should.equal @user.reload.crypted_password
  end
  
  specify 'POST should NOT send email if user is only invited' do
    assert_email_sent 0 do
      post :forgot_password, { :email => @invited_user.email } 
      assert_response :success
      assert_flash(:error)
    end    
  end  
  
  specify 'POST should send email if email valid, and email should contain correct URL' do    
    assert_email_sent do
      post :forgot_password, { :email => @user.email }
      assert_response :redirect
      assert_flash(:notice)
    end

    latest_email.body.should.match "http://8tracks.com/set_password?code="
    
    # password should not change yet    
    @user.crypted_password.should.equal @user.reload.crypted_password
  end
  
  specify 'POST should not send email if email is not matched' do
    assert_email_sent 0 do
      post :forgot_password, { :email=>"invalid" } 
      assert_response :success
      assert_flash_now(:error)
    end    
  end
  
  specify 'POST should redirect if already logged in' do
    login_as @user
    post :forgot_password, { :email => @user.email } 
    should.be.redirected_to edit_user_path(@user)
  end  
    
end




context "Resetting Password", ActionController::TestCase do
  use_controller SessionsController  
  include SessionsTestHelpers
  
  setup do
    setup_shared
    setup_forgot_password
  end
  
  specify 'GET should show form if code is valid' do
    get :set_password, :code => @user.invitation_code
    status.should.be :success
    
    assert_select "form#set_password_form[method=post][action=/set_password]" do
      assert_select "input#user_password"
      assert_select "input[type=submit][value=Set Password]"
    end
  end

  specify 'GET should show redirect if already logged in' do
    login_as @user
    get :set_password, :code => @user.invitation_code
    should.be.redirected_to edit_user_path(@user)
    assert_flash(:notice)
  end

  specify 'GET should show error if code is invalid' do
    get :set_password, :code => 'invalid'
    should.be.redirected_to forgot_password_path
    assert_flash(:error)    
  end


  specify 'PUT should reset password, log in and redirect if code is valid' do
    put :set_password, { :code => @user.invitation_code, :user => { :password => '1234' } }
    should.be.redirected_to home_path
    assert_flash(:notice)
    assert_logged_in(@user)
  end

  specify 'POST should show error if code is invalid' do
    put :set_password, :code => 'invalid'
    should.be.redirected_to forgot_password_path
    assert_flash(:error)    
  end
    
end


