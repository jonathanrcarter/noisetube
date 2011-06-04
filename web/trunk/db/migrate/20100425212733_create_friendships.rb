class CreateFriendships < ActiveRecord::Migration
  def self.up


    add_column :measures, :time_index, :integer
    add_index :measures, :time_index
    
    create_table :followership do |t|
      t.integer :city_id
      t.integer :follower_id
      t.timestamps
    end
  end
  
  def self.down
    drop_table :followership
    remove_column :measures, :time_index
  end
end
