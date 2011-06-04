class AddPhoneModelCity < ActiveRecord::Migration
  def self.up
     add_column :users,:phonemodel, :string , :limit => 40
  end

  def self.down
  end
end
