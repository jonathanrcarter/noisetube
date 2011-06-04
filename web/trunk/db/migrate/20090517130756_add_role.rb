class AddRole < ActiveRecord::Migration
  def self.up
    add_column :users, :role, :string, :default=>"citizen"
    
  end

  def self.down
  end
end
