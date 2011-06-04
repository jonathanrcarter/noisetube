class AddTwitterAccount < ActiveRecord::Migration
  def self.up
    add_column :users, :twitter, :string
  end

  def self.down
  end
end
