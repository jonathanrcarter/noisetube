class CreateFavlocations < ActiveRecord::Migration
  def self.up
    create_table :favlocations do |t|
      t.string :name
      t.float :lon
      t.float :lat
      t.integer :user_id

      t.timestamps
    end
  end

  def self.down
    drop_table :favlocations
  end
end
