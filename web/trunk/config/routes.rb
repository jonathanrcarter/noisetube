# --------------------------------------------------------------------------------
#  NoiseTube Web application
#  
#  Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
#  Portions contributed by Vrije Universiteit Brussel (BrusSense team), 2008-2011
# --------------------------------------------------------------------------------
#  This library is free software; you can redistribute it and/or modify it under
#  the terms of the GNU Lesser General Public License, version 2.1, as published
#  by the Free Software Foundation.
#  
#  This library is distributed in the hope that it will be useful, but WITHOUT
#  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
#  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
#  details.
#  
#  You should have received a copy of the GNU Lesser General Public License along
#  with this library; if not, write to:
#    Free Software Foundation, Inc.,
#    51 Franklin Street, Fifth Floor,
#    Boston, MA  02110-1301, USA.
#  
#  Full GNU LGPL v2.1 text: http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
#  NoiseTube project source code repository: http://code.google.com/p/noisetube
# --------------------------------------------------------------------------------
#  More information:
#   - NoiseTube project website: http://www.noisetube.net
#   - Sony Computer Science Laboratory Paris: http://csl.sony.fr
#   - VUB BrusSense team: http://www.brussense.be
# --------------------------------------------------------------------------------
 
ActionController::Routing::Routes.draw do |map|
  map.resources :followership


  map.root :controller => 'homepage' 
  
  map.logout '/logout', :controller => 'sessions', :action => 'destroy'
  map.login '/login', :controller => 'sessions', :action => 'new'
  map.register '/register', :controller => 'users', :action => 'create'
  map.signup '/signup', :controller => 'users', :action => 'new'
  map.forgot_password '/forgot_password', :controller => 'users', :action => 'forgot_password'
  
  map.reset_password '/reset_password/:id', :controller => 'users', :action => 'reset_password'  
  
  map.resource :session

  map.resources :users , :member => {:profile=>:get, :map => :get, :uploaddata=>:get, :uploadxml=>:post, :tracks=> :get, :measures=> :get}, :collection=>{:realtime =>:get} do |user|
    user.resources :tracks , :member => {:destroy=>:get,:animated_map=>:get}
  end
      
  map.resources :cities
  
  map.resources :taggings, :member => {:measures=>:get}
  
  map.connect '/:action.:format' , :controller => 'homepage' 
  
  # for api + generic
  
  # #map.connect ':controller/:action'
  
  map.connect ':controller/:action.:format'
  
  # The priority is based upon order of creation: first created -> highest
  # priority.

  # Sample of regular route:
  #   map.connect 'products/:id', :controller => 'catalog', :action => 'view'
  # Keep in mind you can assign values other than :controller and :action

  # Sample of named route:
  #   map.purchase 'products/:id/purchase', :controller => 'catalog', :action => 'purchase'
  # This route can be invoked with purchase_url(:id => product.id)

  # Sample resource route (maps HTTP verbs to controller actions automatically):
  #   map.resources :products

  # Sample resource route with options:
  #   map.resources :products, :member => { :short => :get, :toggle => :post }, :collection => { :sold => :get }

  # Sample resource route with sub-resources:
  #   map.resources :products, :has_many => [ :comments, :sales ], :has_one => :seller
  
  # Sample resource route with more complex sub-resources
  #   map.resources :products do |products|
  #     products.resources :comments
  #     products.resources :sales, :collection => { :recent => :get }
  #   end

  # Sample resource route within a namespace:
  #   map.namespace :admin do |admin|
  #     # Directs /admin/products/* to Admin::ProductsController (app/controllers/admin/products_controller.rb)
  #     admin.resources :products
  #   end

  # You can have the root of your site routed with map.root -- just remember to
  # delete public/index.html. map.root :controller => "welcome"

  # See how all your routes lay out with "rake routes"

  # Install the default routes as the lowest priority. Note: These default
  # routes make all actions in every controller accessible via GET requests. You
  # should consider removing the them or commenting them out if you're using
  # named routes and resources.
  map.connect ':controller/:action/:id'
  
end
